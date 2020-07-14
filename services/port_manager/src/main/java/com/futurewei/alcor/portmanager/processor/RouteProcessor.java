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
import com.futurewei.alcor.portmanager.request.*;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

public class RouteProcessor extends AbstractProcessor {
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

    @Override
    void createProcess(List<PortEntity> portEntities) {
        Map<String, SubnetRoute> subnetRoutes = new HashMap<>();

        portEntities.stream().forEach((p) -> {
            List<PortEntity.FixedIp> fixedIps = p.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    String subnetId = fixedIp.getSubnetId();
                    if (!subnetRoutes.containsKey(subnetId)) {
                        SubnetRoute subnetRoute = new SubnetRoute(subnetId, null);
                        subnetRoutes.put(subnetId, subnetRoute);
                    }
                }
            }
        });

        if (subnetRoutes.size() > 0) {
            UpstreamRequest fetchSubnetRouteRequest =
                    new FetchSubnetRouteRequest(new ArrayList<>(subnetRoutes.values()));
            sendRequest(fetchSubnetRouteRequest, this::fetchSubnetRouteCallback);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
