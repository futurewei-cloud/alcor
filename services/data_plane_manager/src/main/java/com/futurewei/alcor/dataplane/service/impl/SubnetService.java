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

import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubnetService extends ResourceService {
    public InternalSubnetEntity getInternalSubnetEntity(NetworkConfiguration networkConfig, String subnetId) throws Exception {
        InternalSubnetEntity result = null;
        for (InternalSubnetEntity internalSubnetEntity: networkConfig.getSubnets()) {
            if (internalSubnetEntity.getId().equals(subnetId)) {
                result = internalSubnetEntity;
            }
        }

        if (result == null) {
            throw new SubnetEntityNotFound();
        }

        return result;
    }

    public void buildSubnetStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            for (Port.PortConfiguration.FixedIp fixedIp: portState.getConfiguration().getFixedIpsList()) {
                InternalSubnetEntity internalSubnetEntity = getInternalSubnetEntity(
                        networkConfig, fixedIp.getSubnetId());
                subnetEntities.add(internalSubnetEntity);
            }
        }

        for (InternalSubnetEntity subnetEntity: subnetEntities) {
            Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
            subnetConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
            subnetConfigBuilder.setId(subnetEntity.getId());
            subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
            subnetConfigBuilder.setVpcId(subnetEntity.getVpcId());
            subnetConfigBuilder.setName(subnetEntity.getName());
            subnetConfigBuilder.setCidr(subnetEntity.getCidr());
            subnetConfigBuilder.setTunnelId(subnetEntity.getTunnelId());

            Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
            gatewayBuilder.setIpAddress(subnetEntity.getGatewayIp());
            gatewayBuilder.setMacAddress(subnetEntity.getGatewayPortDetail().getGatewayMacAddress());
            subnetConfigBuilder.setGateway(gatewayBuilder.build());

            if (subnetEntity.getDhcpEnable() != null) {
                subnetConfigBuilder.setDhcpEnable(subnetEntity.getDhcpEnable());
            }

            // TODO: need to set DNS based on latest contract

            if (subnetEntity.getAvailabilityZone() != null) {
                subnetConfigBuilder.setAvailabilityZone(subnetEntity.getAvailabilityZone());
            }

            Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(networkConfig.getOpType());
            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
        }
    }
}
