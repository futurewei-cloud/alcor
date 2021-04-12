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

import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
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

    public void buildSubnetStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState, MulticastGoalState multicastGoalState) throws Exception {
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
            subnetStateBuilder.setOperationType(Common.OperationType.INFO);
            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
            multicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
        }
    }
}
