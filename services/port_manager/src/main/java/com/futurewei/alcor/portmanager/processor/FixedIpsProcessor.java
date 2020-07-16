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

    private void fetchSubnetCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        boolean afterRandomIp = ((FetchSubnetRequest) (request)).isAfterRandomIp();
        List<PortEntity> assignedIpPorts = request.getContext().getAssignedIpPorts();

        List<InternalSubnetEntity> internalSubnetEntities = new ArrayList<>();
        List<IpAddrRequest> fixedIpAddresses = new ArrayList<>();

        for (SubnetEntity subnetEntity: subnetEntities){
            InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(subnetEntity, null);
            internalSubnetEntities.add(internalSubnetEntity);

            if (!afterRandomIp) {
                for (PortEntity portEntity: assignedIpPorts) {
                    for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                        if (subnetEntity.getId().equals(fixedIp.getSubnetId())) {
                            IpAddrRequest ipAddrRequest = new IpAddrRequest(
                                    IpVersion.IPV4.getVersion(), portEntity.getVpcId(),
                                    fixedIp.getSubnetId(), subnetEntity.getIpV4RangeId(),
                                    fixedIp.getIpAddress(), null);
                            fixedIpAddresses.add(ipAddrRequest);
                        }
                    }
                }
            }
        }

        request.getContext().getNetworkConfig().setSubnetEntities(internalSubnetEntities);

        //Allocate fixed ip addresses
        if (fixedIpAddresses.size() > 0) {
            PortContext context = request.getContext();
            IRestRequest allocateFixedIpRequest = new AllocateFixedIpRequest(context, fixedIpAddresses);

            //Since we are in the thread, can not call sendRequest
            allocateFixedIpRequest.send();
        }
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

    private void allocateRandomIpCallback(IRestRequest request) throws Exception {
        List<IpAddrRequest> ipAddresses = ((AllocateRandomIpRequest) (request)).getIpAddresses();
        PortContext context = request.getContext();

        if (context.getUnassignedIpPorts() == null ||
                ipAddresses.size() != context.getUnassignedIpPorts().size()) {
            throw new AllocateIpAddrException();
        }

        int index = 0;
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();
        for (PortEntity portEntity: context.getUnassignedIpPorts()) {
            IpAddrRequest ipAddrRequest = ipAddresses.get(index++);
            PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(
                    ipAddrRequest.getSubnetId(), ipAddrRequest.getIp());
            portEntity.setFixedIps(Collections.singletonList(fixedIp));

            String subnetId = fixedIp.getSubnetId();
            if (!subnetRoutes.containsKey(subnetId)) {
                SubnetRoute subnetRoute = new SubnetRoute(subnetId, null);
                subnetRoutes.put(subnetId, subnetRoute);
            }
        }

        //Get subnet route
        IRestRequest fetchSubnetRouteRequest =
                new FetchSubnetRouteRequest(context, new ArrayList<>(subnetRoutes.values()));
        fetchSubnetRouteRequest.send();
        fetchSubnetRouteCallback(fetchSubnetRouteRequest);

        //Get subnet
        List<String> subnetIds = subnetRoutes.values().stream()
                .map(SubnetRoute::getSubnetId)
                .collect(Collectors.toList());

        IRestRequest fetchSubnetRequest = new FetchSubnetRequest(context, subnetIds, true);
        fetchSubnetRequest.send();
        fetchSubnetCallBack(fetchSubnetRequest);
    }

    @Override
    void createProcess(PortContext context) {
        Set<String> subnetIds = new HashSet<>();
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();
        List<PortEntity> assignedIpPorts = new ArrayList<>();
        List<PortEntity> noAssignedIpPorts = new ArrayList<>();
        List<IpAddrRequest> randomIpAddresses = new ArrayList<>();

        for (PortEntity portEntity: context.getPortEntities()){
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    subnetIds.add(fixedIp.getSubnetId());
                    if (!subnetRoutes.containsKey(fixedIp.getSubnetId())) {
                        SubnetRoute subnetRoute = new SubnetRoute(
                                fixedIp.getSubnetId(), null);
                        subnetRoutes.put(fixedIp.getSubnetId(), subnetRoute);
                    }
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
        if (subnetIds.size() > 0) {
            IRestRequest fetchSubnetRequest = new FetchSubnetRequest(
                    context, new ArrayList<>(subnetIds), false);
            context.getRequestManager().sendRequestAsync(fetchSubnetRequest, this::fetchSubnetCallBack);
        }

        //Get subnet route
        if (subnetRoutes.size() > 0) {
            IRestRequest fetchSubnetRouteRequest = new FetchSubnetRouteRequest(
                    context, new ArrayList<>(subnetRoutes.values()));
            context.getRequestManager().sendRequestAsync(
                    fetchSubnetRouteRequest, this::fetchSubnetRouteCallback);
        }

        //Allocate random ip addresses
        if (randomIpAddresses.size() > 0) {
            IRestRequest allocateRandomIpRequest =
                    new AllocateRandomIpRequest(context, randomIpAddresses);
            context.getRequestManager().sendRequestAsync(
                    allocateRandomIpRequest, this::allocateRandomIpCallback);
        }
    }

    @Override
    void updateProcess(PortContext context) throws Exception {

    }

    private void fetchSubnetForDeleteCallBack(IRestRequest request) throws Exception {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();
        List<PortEntity> portEntities = request.getContext().getPortEntities();
        List<IpAddrRequest> fixedIpAddresses = new ArrayList<>();

        for (PortEntity portEntity: portEntities) {
            for (SubnetEntity subnetEntity: subnetEntities) {
                for (PortEntity.FixedIp fixedIp : portEntity.getFixedIps()) {
                    if (subnetEntity.getId().equals(fixedIp.getSubnetId())) {
                        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                                IpVersion.IPV4.getVersion(), portEntity.getVpcId(),
                                fixedIp.getSubnetId(), subnetEntity.getIpV4RangeId(),
                                fixedIp.getIpAddress(), null);
                        fixedIpAddresses.add(ipAddrRequest);
                    }
                }
            }
        }

        //Release ip addresses
        if (fixedIpAddresses.size() > 0) {
            IRestRequest releaseFixedIpRequest = new ReleaseIpRequest(request.getContext(), fixedIpAddresses);
            releaseFixedIpRequest.send();
        }
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        Set<String> subnetIds = new HashSet<>();
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();

        for (PortEntity portEntity: context.getPortEntities()) {
            if (portEntity.getFixedIps() == null) {
                continue;
            }

            for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                subnetIds.add(fixedIp.getSubnetId());
                SubnetRoute subnetRoute = new SubnetRoute(
                        fixedIp.getSubnetId(), null);
                subnetRoutes.put(fixedIp.getSubnetId(), subnetRoute);
            }
        }

        //Get subnet
        if (subnetIds.size() > 0) {
            IRestRequest fetchSubnetRequest = new FetchSubnetRequest(
                    context, new ArrayList<>(subnetIds), false);
            context.getRequestManager().sendRequestAsync(
                    fetchSubnetRequest, this::fetchSubnetForDeleteCallBack);
        }

        //Get subnet route
        if (subnetRoutes.size() > 0) {
            IRestRequest fetchSubnetRouteRequest = new FetchSubnetRouteRequest(
                    context, new ArrayList<>(subnetRoutes.values()));
            context.getRequestManager().sendRequestAsync(
                    fetchSubnetRouteRequest, this::fetchSubnetRouteCallback);
        }
    }
}
