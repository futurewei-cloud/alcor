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

    private void setSubnetEntities(PortContext context, List<SubnetEntity> subnetEntities) {
        List<InternalSubnetEntity> internalSubnetEntities = new ArrayList<>();
        for (SubnetEntity subnetEntity: subnetEntities) {
            InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(subnetEntity, null);
            internalSubnetEntities.add(internalSubnetEntity);
        }

        context.getNetworkConfig().setSubnetEntities(internalSubnetEntities);
    }

    private void allocateFixedIpAddress(PortContext context, List<IpAddrRequest> ipAddresses) throws Exception {
        IRestRequest allocateFixedIpRequest = new AllocateFixedIpRequest(context, ipAddresses);
        allocateFixedIpRequest.send();
    }

    private void releaseFixedIpAddress(PortContext context, List<IpAddrRequest> ipAddresses) throws Exception {
        IRestRequest releaseIpRequest = new ReleaseIpRequest(context, ipAddresses);
        releaseIpRequest.send();
    }

    private void allocateOrReleaseIpAddresses(PortContext context, List<SubnetEntity> subnetEntities,
                                              List<PortEntity> portEntities, IpAddrRequestFunction function) throws Exception {
        if (function == null) {
            return;
        }

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        for (SubnetEntity subnetEntity: subnetEntities){
            for (PortEntity portEntity: portEntities) {
                for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                    if (subnetEntity.getId().equals(fixedIp.getSubnetId())) {
                        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                                IpVersion.IPV4.getVersion(), portEntity.getVpcId(),
                                fixedIp.getSubnetId(), subnetEntity.getIpV4RangeId(),
                                fixedIp.getIpAddress(), null);
                        ipAddrRequests.add(ipAddrRequest);
                    }
                }
            }
        }

        function.apply(context, ipAddrRequests);
    }

    private void fetchSubnetForAddCallBack(IRestRequest request) throws Exception {
        PortContext context = request.getContext();
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        boolean allocateIpAddress = ((FetchSubnetRequest) (request)).isAllocateIpAddress();
        List<PortEntity> assignedIpPorts = request.getContext().getAssignedIpPorts();
        IpAddrRequestFunction function = allocateIpAddress ? this::allocateFixedIpAddress: null;

        setSubnetEntities(context, subnetEntities);
        allocateOrReleaseIpAddresses(context, subnetEntities, assignedIpPorts, function);
    }

    private void fetchSubnetForUpdateCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        boolean allocateIpAddress = !((FetchSubnetRequest) (request)).isAllocateIpAddress();
        PortContext context = request.getContext();
        IpAddrRequestFunction function;
        PortEntity portEntity;

        if (allocateIpAddress) {
            setSubnetEntities(context, subnetEntities);
            function = this::allocateFixedIpAddress;
            portEntity = request.getContext().getNewPortEntity();
        } else {
            function = this::releaseFixedIpAddress;
            portEntity = request.getContext().getOldPortEntity();
        }

        allocateOrReleaseIpAddresses(context, subnetEntities, Collections.singletonList(portEntity), function);
    }

    private void fetchSubnetForDeleteCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        List<PortEntity> portEntities = request.getContext().getPortEntities();
        PortContext context = request.getContext();

        setSubnetEntities(context, subnetEntities);
        allocateOrReleaseIpAddresses(context, subnetEntities, portEntities, this::releaseFixedIpAddress);
    }

    private void fetchSubnetRouteCallback(IRestRequest request) {
        List<SubnetRoute> subnetRoutes = ((FetchSubnetRouteRequest) request).getSubnetRoutes();

        NetworkConfig networkConfig = request.getContext().getNetworkConfig();
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (InternalPortEntity internalPortEntity: internalPortEntities) {
            for (SubnetRoute subnetRoute: subnetRoutes) {
                List<PortEntity.FixedIp> fixedIps = internalPortEntity.getFixedIps();
                if (fixedIpsContainSubnet(fixedIps, subnetRoute.getSubnetId())) {
                    internalPortEntity.setRoutes(subnetRoute.getRouteEntities());
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

    private void allocateRandomIpAddresses(PortContext context, List<IpAddrRequest> randomIpAddresses) {
        if (randomIpAddresses.size() > 0) {
            IRestRequest allocateRandomIpRequest =
                    new AllocateRandomIpRequest(context, randomIpAddresses);
            context.getRequestManager().sendRequestAsync(
                    allocateRandomIpRequest, this::allocateRandomIpCallback);
        }
    }

    private void allocateRandomIpCallback(IRestRequest request) throws Exception {
        List<IpAddrRequest> ipAddresses = ((AllocateRandomIpRequest) (request)).getIpAddresses();
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

    @Override
    void createProcess(PortContext context) {
        Set<String> subnetIds = new HashSet<>();
        List<PortEntity> assignedIpPorts = new ArrayList<>();
        List<PortEntity> noAssignedIpPorts = new ArrayList<>();
        List<IpAddrRequest> randomIpAddresses = new ArrayList<>();

        for (PortEntity portEntity: context.getPortEntities()){
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    subnetIds.add(fixedIp.getSubnetId());
                }
                assignedIpPorts.add(portEntity);
            } else {
                noAssignedIpPorts.add(portEntity);
                IpAddrRequest ipAddrRequest = new IpAddrRequest(
                        IpVersion.IPV4.getVersion(), portEntity.getVpcId(),
                        null, null, null, null);
                randomIpAddresses.add(ipAddrRequest);
            }
        }

        context.setAssignedIpPorts(assignedIpPorts);
        context.setUnassignedIpPorts(noAssignedIpPorts);

        //Get subnet
        getSubnetEntities(context, new ArrayList<>(subnetIds), true, this::fetchSubnetForAddCallBack);

        //Get subnet route
        getSubnetRoutes(context, new ArrayList<>(subnetIds));

        //Allocate random ip addresses
        allocateRandomIpAddresses(context, randomIpAddresses);
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        List<PortEntity.FixedIp> newFixedIps = newPortEntity.getFixedIps();
        List<PortEntity.FixedIp> oldFixedIps = oldPortEntity.getFixedIps();

        if (newFixedIps != null && !newFixedIps.equals(oldFixedIps)) {
            if (newFixedIps.size() > 0) {
                Set<String> subnetIds = new HashSet<>();
                Map<String, SubnetRoute> subnetRoutes = new HashMap<>();

                for (PortEntity.FixedIp fixedIp: newFixedIps) {
                    subnetIds.add(fixedIp.getSubnetId());
                }

                //Get subnet
                getSubnetEntities(context, new ArrayList<>(subnetIds), true, this::fetchSubnetForUpdateCallBack);

                //Get subnet route
                getSubnetRoutes(context, new ArrayList<>(subnetIds));
            }

            if (oldFixedIps.size() > 0) {
                List<String> subnetIds = oldFixedIps.stream()
                        .map(PortEntity.FixedIp::getSubnetId)
                        .collect(Collectors.toList());

                getSubnetEntities(context, subnetIds, false, this::fetchSubnetForUpdateCallBack);
            }

            oldPortEntity.setFixedIps(newFixedIps);
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
