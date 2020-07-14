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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrivateIpProcessor extends AbstractProcessor {
    private List<PortEntity> targetPorts = new ArrayList<>();

    private boolean checkPortHashSubnetId(PortEntity portEntity, String subnetId) {
        if (portEntity.getFixedIps() == null) {
            return false;
        }

        for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
            if (fixedIp.getSubnetId().equals(subnetId)) {
                return true;
            }
        }

        return false;
    }

    private void fetchSubnetRouteCallback(UpstreamRequest request) {
        List<SubnetRoute> subnetRoutes = ((FetchSubnetRouteRequest) request).getSubnetRoutes();

        List<NetworkConfig.ExtendPortEntity> extendPortEntities = networkConfig.getPortEntities();
        for (NetworkConfig.ExtendPortEntity extendPortEntity: extendPortEntities) {
            for (SubnetRoute subnetRoute: subnetRoutes) {
                if (checkPortHashSubnetId(extendPortEntity, subnetRoute.getSubnetId())) {
                    extendPortEntity.getInternalPortEntity().setRoutes(subnetRoute.getRouteEntities());
                }
            }
        }
    }

    private void fetchSubnetCallBack(UpstreamRequest request) {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();

        List<InternalSubnetEntity> internalSubnetEntities = new ArrayList<>();
        subnetEntities.stream().forEach((s) -> {
            InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(s, null);
            internalSubnetEntities.add(internalSubnetEntity);
        });

        networkConfig.setSubnetEntities(internalSubnetEntities);
    }

    private void allocateRandomIpCallback(UpstreamRequest request) throws AllocateIpAddrException {
        List<IpAddrRequest> ipAddresses = ((AllocateRandomIpRequest) (request)).getIpAddresses();

        if (ipAddresses.size() != targetPorts.size()) {
            throw new AllocateIpAddrException();
        }

        int index = 0;
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();
        for (PortEntity portEntity: targetPorts) {
            IpAddrRequest ipAddrRequest = ipAddresses.get(index++);
            PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(
                    ipAddrRequest.getSubnetId(), ipAddrRequest.getIp());
            List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
            fixedIps.add(fixedIp);

            portEntity.setFixedIps(fixedIps);

            String subnetId = fixedIp.getSubnetId();
            if (!subnetRoutes.containsKey(subnetId)) {
                SubnetRoute subnetRoute = new SubnetRoute(subnetId, null);
                subnetRoutes.put(subnetId, subnetRoute);
            }
        }

        //Get subnet route
        UpstreamRequest fetchSubnetRouteRequest =
                new FetchSubnetRouteRequest(new ArrayList<>(subnetRoutes.values()));
        sendRequest(fetchSubnetRouteRequest, this::fetchSubnetRouteCallback);

        //Get subnet
        List<String> subnetIds = subnetRoutes.values().stream()
                .map(SubnetRoute::getSubnetId)
                .collect(Collectors.toList());

        UpstreamRequest fetchSubnetRequest = new FetchSubnetRequest(projectId, subnetIds);
        sendRequest(fetchSubnetRequest, this::fetchSubnetCallBack);
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        List<IpAddrRequest> fixedIpAddresses = new ArrayList<>();
        List<IpAddrRequest> randomIpAddresses = new ArrayList<>();

        portEntities.stream().forEach((p) -> {
            List<PortEntity.FixedIp> fixedIps = p.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    IpAddrRequest ipAddrRequest = new IpAddrRequest(4, p.getVpcId(),
                            fixedIp.getSubnetId(), null,
                            fixedIp.getIpAddress(), null);
                    fixedIpAddresses.add(ipAddrRequest);
                }
            } else {
                targetPorts.add(p);
                IpAddrRequest ipAddrRequest = new IpAddrRequest(
                        IpVersion.IPV4.getVersion(), p.getVpcId(),
                        null, null, null, null);
                fixedIpAddresses.add(ipAddrRequest);
            }
        });

        //Allocate fixed ip addresses
        if (fixedIpAddresses.size() > 0) {
            UpstreamRequest allocateFixedIpRequest = new AllocateFixedIpRequest(fixedIpAddresses);
            sendRequest(allocateFixedIpRequest, null);
        }


        //Allocate random ip addresses
        if (randomIpAddresses.size() > 0) {
            UpstreamRequest allocateRandomIpRequest = new AllocateRandomIpRequest(randomIpAddresses);
            sendRequest(allocateRandomIpRequest, this::allocateRandomIpCallback);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
