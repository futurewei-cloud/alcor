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
package com.futurewei.alcor.dataplane.service.ovs;

import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.VpcEntityNotFound;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Vpc;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VpcService extends ResourceService {
    private VpcEntity getVpcEntity(NetworkConfiguration networkConfig, String vpcId) throws Exception {
        VpcEntity result = null;
        for (VpcEntity vpcEntity: networkConfig.getVpcs()) {
            if (vpcEntity.getId().equals(vpcId)) {
                result = vpcEntity;
            }
        }

        if (result == null) {
            throw new VpcEntityNotFound();
        }

        return result;
    }

    public void buildVpcStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (Port.PortState portState: portStates) {
            VpcEntity vpcEntity = getVpcEntity(networkConfig, portState.getConfiguration().getVpcId());
            Vpc.VpcConfiguration.Builder vpcConfigBuilder = Vpc.VpcConfiguration.newBuilder();
            vpcConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
            vpcConfigBuilder.setId(vpcEntity.getId());
            vpcConfigBuilder.setProjectId(vpcEntity.getProjectId());

            if (vpcEntity.getName() != null) {
                vpcConfigBuilder.setName(vpcEntity.getName());
            }

            if (vpcEntity.getCidr() != null) {
                vpcConfigBuilder.setCidr(vpcEntity.getCidr());
            }

            //vpcConfigBuilder.setTunnelId();

            if (networkConfig.getSubnets() != null) {
                networkConfig.getSubnets().stream()
                        .filter(s -> s.getVpcId().equals(vpcEntity.getId()))
                        .map(InternalSubnetEntity::getId)
                        .forEach(id -> {
                            Vpc.VpcConfiguration.SubnetId.Builder subnetIdBuilder = Vpc.VpcConfiguration.SubnetId.newBuilder();
                            subnetIdBuilder.setId(id);
                            vpcConfigBuilder.addSubnetIds(subnetIdBuilder.build());
                        });
            }
            //set routes here

            Vpc.VpcState.Builder vpcStateBuilder = Vpc.VpcState.newBuilder();
            vpcStateBuilder.setOperationType(networkConfig.getOpType());
            vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

            unicastGoalState.getGoalStateBuilder().addVpcStates(vpcStateBuilder.build());
        }
    }
}
