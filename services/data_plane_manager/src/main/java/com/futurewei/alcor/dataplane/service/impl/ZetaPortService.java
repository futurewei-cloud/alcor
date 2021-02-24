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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.entity.ZetaPortGoalState;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.gateway.ZetaPortEntity;
import com.futurewei.alcor.web.entity.gateway.ZetaPortIP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZetaPortService extends ResourceService {
    @Value("${zetaGateway.node.mac}")
    private String zetaGatewayNodeMac;

    public void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities,
                               ZetaPortGoalState zetaPortGoalState) {
        List<ZetaPortEntity> zetaPortEntities = new ArrayList<>();
        zetaPortGoalState.setOpType(networkConfig.getOpType());
        for (InternalPortEntity portEntity: portEntities) {
            List<ZetaPortIP> zetaPortIPs = new ArrayList<>();
            if (portEntity.getFixedIps() != null) {
                portEntity.getFixedIps().forEach(fixedIp -> {
                    zetaPortIPs.add(new ZetaPortIP(fixedIp.getIpAddress(), ""));
                });
            }

            if (portEntity.getIsZetaGatewayPort()) {
                ZetaPortEntity zetaPortEntity = new ZetaPortEntity(portEntity.getId(), portEntity.getVpcId(), zetaPortIPs,
                        portEntity.getMacAddress(), portEntity.getBindingHostIP(), zetaGatewayNodeMac);
                zetaPortEntities.add(zetaPortEntity);
            }
        }
        zetaPortGoalState.setPortEntities(zetaPortEntities);
    }
}
