/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
