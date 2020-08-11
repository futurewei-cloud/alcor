/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.entity.SubnetRoute;
import com.futurewei.alcor.portmanager.exception.AllocateIpAddrException;
import com.futurewei.alcor.portmanager.request.*;
import com.futurewei.alcor.portmanager.util.ArrayUtil;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;

import java.util.*;
import java.util.stream.Collectors;

public class FixedIpsProcessor extends AbstractProcessor {
    private boolean fixedIpsContainSubnet(List<PortEntity.FixedIp> fixedIps, String subnetId) {
        if (fixedIps == null) {
            return false;
        }

        for (PortEntity.FixedIp fixedIp: fixedIps) {
            if (fixedIp.getSubnetId().equals(subnetId)) {
                return true;
            }
        }

        return false;
    }

    @FunctionalInterface
    private interface IpAddrRequestFunction {
        void apply(PortContext context, List<IpAddrRequest> ipAddresses) throws Exception;
    }

    private void addSubnetEntities(PortContext context, List<SubnetEntity> subnetEntities) {
        List<InternalSubnetEntity> internalSubnetEntities = new ArrayList<>();
        for (SubnetEntity subnetEntity: subnetEntities) {
            InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(subnetEntity, null);
            internalSubnetEntities.add(internalSubnetEntity);
        }

        if (context.getNetworkConfig().getSubnetEntities() == null) {
            context.getNetworkConfig().setSubnetEntities(internalSubnetEntities);
        } else {
            context.getNetworkConfig().getSubnetEntities().addAll(internalSubnetEntities);
        }
    }

    private void allocateFixedIpAddress(PortContext context, List<IpAddrRequest> ipAddresses) throws Exception {
        AllocateIpAddressRequest allocateIpAddressRequest = new AllocateIpAddressRequest(context, ipAddresses);
        allocateIpAddressRequest.send();

        List<IpAddrRequest> result = allocateIpAddressRequest.getResult();
        Iterator<IpAddrRequest> iterator = result.iterator();

        List<String> filterIps = context.getHasSubnetFixedIps().stream()
                .filter(f -> f.getIpAddress() != null)
                .map(PortEntity.FixedIp::getIpAddress)
                .collect(Collectors.toList());

        while (iterator.hasNext()) {
            IpAddrRequest ipAddr = iterator.next();
            if (filterIps.contains(ipAddr.getIp())) {
                iterator.remove();
                continue;
            }

            for (PortEntity.FixedIp fixedIp: context.getHasSubnetFixedIps()) {
                if (fixedIp.getSubnetId().equals(ipAddr.getSubnetId())) {
                    fixedIp.setIpAddress(ipAddr.getIp());
                }
            }
        }

        //Check if all fixedIps have been assigned ip address
        for (PortEntity.FixedIp fixedIp: context.getHasSubnetFixedIps()) {
            if (fixedIp.getIpAddress() == null) {
                throw new AllocateIpAddrException();
            }
        }
    }

    private void releaseFixedIpAddress(PortContext context, List<IpAddrRequest> ipAddresses) throws Exception {
        ipAddresses.removeIf(s -> s.getIp() == null);

        if (ipAddresses.size() > 0) {
            IRestRequest releaseIpRequest = new ReleaseIpAddressRequest(context, ipAddresses);
            releaseIpRequest.send();
        }
    }

    private void postFetchSubnet(PortContext context, List<SubnetEntity> subnetEntities,
                                 List<PortEntity.FixedIp> fixedIps, IpAddrRequestFunction function) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        for (SubnetEntity subnetEntity: subnetEntities) {
            for (PortEntity.FixedIp fixedIp: fixedIps) {
                if (fixedIp.getSubnetId().equals(subnetEntity.getId())) {
                    ipAddrRequests.add(new IpAddrRequest(IpVersion.IPV4.getVersion(),
                            subnetEntity.getVpcId(),
                            fixedIp.getSubnetId(),
                            subnetEntity.getIpV4RangeId(),
                            fixedIp.getIpAddress(),
                            null));
                }
            }
        }

        function.apply(context, ipAddrRequests);
    }

    private void fetchSubnetCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        PortContext context = request.getContext();
        addSubnetEntities(context, subnetEntities);
    }

    private void fetchSubnetForAddCallBack(IRestRequest request) throws Exception {
        PortContext context = request.getContext();
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        boolean allocateIpAddress = ((FetchSubnetRequest) (request)).isAllocateIpAddress();
        List<PortEntity.FixedIp> fixedIps = request.getContext().getHasSubnetFixedIps();

        addSubnetEntities(context, subnetEntities);

        if (allocateIpAddress) {
            postFetchSubnet(context, subnetEntities, fixedIps, this::allocateFixedIpAddress);
        }
    }

    private void fetchSubnetForUpdateCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        boolean allocateIpAddress = !((FetchSubnetRequest) (request)).isAllocateIpAddress();
        PortContext context = request.getContext();
        IpAddrRequestFunction function;
        PortEntity portEntity;

        if (allocateIpAddress) {
            addSubnetEntities(context, subnetEntities);
            function = this::allocateFixedIpAddress;
            portEntity = request.getContext().getNewPortEntity();
        } else {
            function = this::releaseFixedIpAddress;
            portEntity = request.getContext().getOldPortEntity();
        }

        postFetchSubnet(context, subnetEntities, portEntity.getFixedIps(), function);
    }

    private void fetchSubnetForDeleteCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        List<PortEntity> portEntities = request.getContext().getPortEntities();
        PortContext context = request.getContext();

        addSubnetEntities(context, subnetEntities);

        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        portEntities.forEach(p -> {
            fixedIps.addAll(p.getFixedIps());
        });
        postFetchSubnet(context, subnetEntities, fixedIps, this::releaseFixedIpAddress);
    }

    private void fetchSubnetRouteCallback(IRestRequest request) {
        List<SubnetRoute> subnetRoutes = ((FetchSubnetRouteRequest) request).getSubnetRoutes();

        NetworkConfig networkConfig = request.getContext().getNetworkConfig();
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (InternalPortEntity internalPortEntity: internalPortEntities) {
            for (SubnetRoute subnetRoute: subnetRoutes) {
                List<PortEntity.FixedIp> fixedIps = internalPortEntity.getFixedIps();
                if (fixedIpsContainSubnet(fixedIps, subnetRoute.getSubnetId())) {
                    if (internalPortEntity.getRoutes() == null) {
                        internalPortEntity.setRoutes(new ArrayList<>());
                    }

                    internalPortEntity.getRoutes().addAll(subnetRoute.getRouteEntities());
                }
            }
        }
    }

    private void getSubnetEntities(PortContext context, List<String> subnetIds, boolean allocateIp, CallbackFunction callback) {
        if (subnetIds.size() > 0) {
            IRestRequest fetchSubnetRequest = new FetchSubnetRequest(
                    context, new ArrayList<>(subnetIds), allocateIp);
            context.getRequestManager().sendRequestAsync(fetchSubnetRequest, callback);
        }
     }

    private void getSubnetRoutes(PortContext context, List<String> subnetIds) {
        if (subnetIds.size() > 0) {
            IRestRequest fetchSubnetRouteRequest = new FetchSubnetRouteRequest(context, subnetIds);
            context.getRequestManager().sendRequestAsync(
                    fetchSubnetRouteRequest, this::fetchSubnetRouteCallback);
        }
    }

    private void getSubnetAndRoute(PortContext context, List<String> subnetIds) throws Exception {
        //Get subnet route
        IRestRequest fetchSubnetRouteRequest =
                new FetchSubnetRouteRequest(context, new ArrayList<>(subnetIds));
        fetchSubnetRouteRequest.send();
        fetchSubnetRouteCallback(fetchSubnetRouteRequest);

        //Get subnet
        IRestRequest fetchSubnetRequest =
                new FetchSubnetRequest(context, new ArrayList<>(subnetIds), false);
        fetchSubnetRequest.send();
        fetchSubnetForAddCallBack(fetchSubnetRequest);
    }

    private void allocateFixedIpCallback(IRestRequest request) throws Exception {
        List<IpAddrRequest> ipAddresses = ((AllocateIpAddressRequest) (request)).getResult();
        PortContext context = request.getContext();

        Map<String, PortEntity.FixedIp> hasIpFixedIps = context.getHasIpFixedIps();
        Iterator<IpAddrRequest> iterator = ipAddresses.iterator();

        while (iterator.hasNext()) {
            IpAddrRequest ipAddr = iterator.next();
            PortEntity.FixedIp fixedIp = hasIpFixedIps.get(ipAddr.getIp());
            if (fixedIp != null) {
                fixedIp.setSubnetId(ipAddr.getSubnetId());
            }
        }

        //Check if all ipFixedIps have get subnet id
        for (PortEntity.FixedIp fixedIp: hasIpFixedIps.values()) {
            if (fixedIp.getSubnetId() == null) {
                throw new AllocateIpAddrException();
            }
        }

        Set<String> subnetIds = hasIpFixedIps.values().stream()
                .map(PortEntity.FixedIp::getSubnetId)
                .collect(Collectors.toSet());

        getSubnetAndRoute(context, new ArrayList<>(subnetIds));
    }

    private void allocateIpAddresses(PortContext context, List<IpAddrRequest> ipAddresses, CallbackFunction callback) {
        if (ipAddresses.size() > 0) {
            IRestRequest allocateRandomIpRequest =
                    new AllocateIpAddressRequest(context, ipAddresses);
            context.getRequestManager().sendRequestAsync(allocateRandomIpRequest, callback);
        }
    }

    private void allocateRandomIpCallback(IRestRequest request) throws Exception {
        List<IpAddrRequest> ipAddresses = ((AllocateIpAddressRequest) (request)).getResult();
        PortContext context = request.getContext();

        if (context.getUnassignedIpPorts() == null ||
                ipAddresses.size() != context.getUnassignedIpPorts().size()) {
            throw new AllocateIpAddrException();
        }

        int index = 0;
        Set<String> subnetIds = new HashSet<>();
        for (PortEntity portEntity: context.getUnassignedIpPorts()) {
            IpAddrRequest ipAddrRequest = ipAddresses.get(index++);
            PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(
                    ipAddrRequest.getSubnetId(), ipAddrRequest.getIp());
            portEntity.setFixedIps(Collections.singletonList(fixedIp));

            subnetIds.add(fixedIp.getSubnetId());
        }

        getSubnetAndRoute(context, new ArrayList<>(subnetIds));
    }

    private void allocateFixedIpsProcess(PortContext context, List<PortEntity> portEntities, CallbackFunction fetchSubnetCallback) {
        Set<String> subnetIds = new HashSet<>();
        List<PortEntity> noAssignedIpPorts = new ArrayList<>();
        List<IpAddrRequest> fixedIpAddresses = new ArrayList<>();
        List<IpAddrRequest> randomIpAddresses = new ArrayList<>();
        Map<String, PortEntity.FixedIp> hasIpFixedIps = new HashMap<>();
        List<PortEntity.FixedIp> hasSubnetFixedIps = new ArrayList<>();

        for (PortEntity portEntity: portEntities){
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    if (fixedIp.getSubnetId() != null) {
                        subnetIds.add(fixedIp.getSubnetId());
                        hasSubnetFixedIps.add(fixedIp);
                    } else {
                        fixedIpAddresses.add(new IpAddrRequest(IpVersion.IPV4.getVersion(),
                                portEntity.getVpcId(),
                                null,
                                null,
                                fixedIp.getIpAddress(),
                                null));
                        hasIpFixedIps.put(fixedIp.getIpAddress(), fixedIp);
                    }
                }
            } else {
                randomIpAddresses.add(new IpAddrRequest(IpVersion.IPV4.getVersion(),
                        portEntity.getVpcId(),
                        null,
                        null,
                        null,
                        null));
                noAssignedIpPorts.add(portEntity);
            }
        }

        context.setHasIpFixedIps(hasIpFixedIps);
        context.setHasSubnetFixedIps(hasSubnetFixedIps);
        context.setUnassignedIpPorts(noAssignedIpPorts);

        //Get subnet
        getSubnetEntities(context, new ArrayList<>(subnetIds), true, this::fetchSubnetForAddCallBack);

        //Get subnet route
        getSubnetRoutes(context, new ArrayList<>(subnetIds));

        //Allocate fixed ip addresses
        allocateIpAddresses(context, fixedIpAddresses, this::allocateFixedIpCallback);

        //Allocate random ip addresses
        allocateIpAddresses(context, randomIpAddresses, this::allocateRandomIpCallback);
    }

    @Override
    void createProcess(PortContext context) {
        allocateFixedIpsProcess(context, context.getPortEntities(), this::fetchSubnetForAddCallBack);
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        List<PortEntity.FixedIp> newFixedIps = newPortEntity.getFixedIps();
        List<PortEntity.FixedIp> oldFixedIps = oldPortEntity.getFixedIps();

        if (newFixedIps != null && newFixedIps.size() > 0) {
            oldPortEntity.setFixedIps(newFixedIps);

            List<PortEntity.FixedIp> commonFixedIps = ArrayUtil.findCommonItems(newFixedIps, oldFixedIps);

            //Allocate new ip addresses
            if (newFixedIps.size() > 0) {
                allocateFixedIpsProcess(context, Collections.singletonList(newPortEntity),
                        this::fetchSubnetForUpdateCallBack);
            }

            //Delete old ip addresses
            if (oldFixedIps.size() > 0) {
                List<String> subnetIds = oldFixedIps.stream()
                        .map(PortEntity.FixedIp::getSubnetId)
                        .collect(Collectors.toList());

                getSubnetEntities(context, subnetIds, false, this::fetchSubnetForUpdateCallBack);
            }

            //Get subnet and subnet route
            if (commonFixedIps.size() > 0) {
                List<String> subnetIds = commonFixedIps.stream()
                        .map(PortEntity.FixedIp::getSubnetId)
                        .collect(Collectors.toList());
                getSubnetEntities(context, subnetIds, false, this::fetchSubnetCallBack);
                getSubnetRoutes(context, new ArrayList<>(subnetIds));
            }
        } else if (oldFixedIps != null && oldFixedIps.size() > 0) {
            List<String> subnetIds = oldFixedIps.stream()
                    .map(PortEntity.FixedIp::getSubnetId)
                    .collect(Collectors.toList());
            getSubnetEntities(context, subnetIds, false, this::fetchSubnetCallBack);
            getSubnetRoutes(context, new ArrayList<>(subnetIds));
        }
    }

    @Override
    void deleteProcess(PortContext context) {
        Set<String> subnetIds = new HashSet<>();
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();

        for (PortEntity portEntity: context.getPortEntities()) {
            if (portEntity.getFixedIps() == null) {
                continue;
            }

            for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                subnetIds.add(fixedIp.getSubnetId());
            }
        }

        //Get subnet
        getSubnetEntities(context, new ArrayList<>(subnetIds), false, this::fetchSubnetForDeleteCallBack);

        //Get subnet route
        getSubnetRoutes(context, new ArrayList<>(subnetIds));
    }
}
