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

import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortService extends ResourceService {
    public void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities,
                               UnicastGoalState unicastGoalState) {
        for (InternalPortEntity portEntity : portEntities) {
            Port.PortConfiguration.Builder portConfigBuilder = Port.PortConfiguration.newBuilder();
            portConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
            portConfigBuilder.setId(portEntity.getId());
            portConfigBuilder.setUpdateType(Common.UpdateType.FULL);
            portConfigBuilder.setVpcId(portEntity.getVpcId());

            if (portEntity.getName() != null) {
                portConfigBuilder.setName(portEntity.getName());
            }

            portConfigBuilder.setDeviceId(portEntity.getDeviceId());
            portConfigBuilder.setDeviceOwner(portEntity.getDeviceOwner());

            portConfigBuilder.setMacAddress(portEntity.getMacAddress());
            boolean adminState = portEntity.getAdminStateUp() == null ? false : portEntity.getAdminStateUp();
            portConfigBuilder.setAdminStateUp(adminState);

            Port.PortConfiguration.HostInfo.Builder hostInfoBuilder = Port.PortConfiguration.HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //TODO: Do we need mac address?
            //hostInfoBuilder.setMacAddress()
            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
            if (portEntity.getFixedIps() != null) {
                portEntity.getFixedIps().forEach(fixedIp -> {
                    Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
                    fixedIpBuilder.setSubnetId(fixedIp.getSubnetId());
                    fixedIpBuilder.setIpAddress(fixedIp.getIpAddress());
                    portConfigBuilder.addFixedIps(fixedIpBuilder.build());
                });
            }

            if (portEntity.getAllowedAddressPairs() != null) {
                portEntity.getAllowedAddressPairs().forEach(pair -> {
                    Port.PortConfiguration.AllowAddressPair.Builder allowAddressPairBuilder = Port.PortConfiguration.AllowAddressPair.newBuilder();
                    allowAddressPairBuilder.setIpAddress(pair.getIpAddress());
                    allowAddressPairBuilder.setMacAddress(pair.getMacAddress());
                    portConfigBuilder.addAllowAddressPairs(allowAddressPairBuilder.build());
                });
            }

            if (portEntity.getSecurityGroups() != null) {
                portEntity.getSecurityGroups().forEach(securityGroupId -> {
                    Port.PortConfiguration.SecurityGroupId.Builder securityGroupIdBuilder = Port.PortConfiguration.SecurityGroupId.newBuilder();
                    securityGroupIdBuilder.setId(securityGroupId);
                    portConfigBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
                });
            }

            //PortState
            Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
            Common.OperationType portOpTypeToACA = determinePortOperationType(portEntity, networkConfig.getOpType());
            portStateBuilder.setOperationType(portOpTypeToACA);
            portStateBuilder.setConfiguration(portConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addPortStates(portStateBuilder.build());
        }
    }

    private Common.OperationType determinePortOperationType(InternalPortEntity portEntity, Common.OperationType operationTypeFromClient) {

        // This is a temporary fix to address Issue #103.
        // TODO:
        //  1. Network configuration entity needs to include a resource-level operation type, not on message level
        //  2. PM determines the operation type for port
        if (operationTypeFromClient == Common.OperationType.UPDATE) {
            if (!Strings.isNullOrEmpty(portEntity.getDeviceId()) && !Strings.isNullOrEmpty(portEntity.getDeviceOwner())) {
                return Common.OperationType.CREATE;
            } else if (Strings.isNullOrEmpty(portEntity.getDeviceId()) && Strings.isNullOrEmpty(portEntity.getDeviceOwner())) {
                return Common.OperationType.DELETE;
            }
        }

        return operationTypeFromClient;
    }

    public void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities,
                               UnicastGoalStateV2 unicastGoalState) {
        for (InternalPortEntity portEntity : portEntities) {
            Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
            Common.OperationType portOpTypeToACA = determinePortOperationType(portEntity, networkConfig.getOpType());
            portStateBuilder.setOperationType(portOpTypeToACA);

            Port.PortConfiguration.Builder portConfigBuilder = portStateBuilder.getConfigurationBuilder();
            portConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER)
                    .setId(portEntity.getId())
                    .setUpdateType(Common.UpdateType.FULL)
                    .setVpcId(portEntity.getVpcId())
                    .setDeviceId(portEntity.getDeviceId())
                    .setDeviceOwner(portEntity.getDeviceOwner())
                    .setMacAddress(portEntity.getMacAddress());

            if (portEntity.getName() != null) {
                portConfigBuilder.setName(portEntity.getName());
            }

            boolean adminState = portEntity.getAdminStateUp() == null ? false : portEntity.getAdminStateUp();
            portConfigBuilder.setAdminStateUp(adminState);

            // TODO: Do we need still HostInfo here which is not exists in pseudo_controller
            /*
            Port.PortConfiguration.HostInfo.Builder hostInfoBuilder = Port.PortConfiguration.HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //hostInfoBuilder.setMacAddress()
            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
            */

            if (portEntity.getFixedIps() != null) {
                portEntity.getFixedIps().forEach(fixedIp -> {
                    Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
                    fixedIpBuilder.setSubnetId(fixedIp.getSubnetId());
                    fixedIpBuilder.setIpAddress(fixedIp.getIpAddress());
                    portConfigBuilder.addFixedIps(fixedIpBuilder.build());
                });
            }

            if (portEntity.getAllowedAddressPairs() != null) {
                portEntity.getAllowedAddressPairs().forEach(pair -> {
                    Port.PortConfiguration.AllowAddressPair.Builder allowAddressPairBuilder = Port.PortConfiguration.AllowAddressPair.newBuilder();
                    allowAddressPairBuilder.setIpAddress(pair.getIpAddress());
                    allowAddressPairBuilder.setMacAddress(pair.getMacAddress());
                    portConfigBuilder.addAllowAddressPairs(allowAddressPairBuilder.build());
                });
            }

            if (portEntity.getSecurityGroups() != null) {
                portEntity.getSecurityGroups().forEach(securityGroupId -> {
                    Port.PortConfiguration.SecurityGroupId.Builder securityGroupIdBuilder = Port.PortConfiguration.SecurityGroupId.newBuilder();
                    securityGroupIdBuilder.setId(securityGroupId);
                    portConfigBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
                });
            }

            portStateBuilder.setConfiguration(portConfigBuilder.build());
            Port.PortState portState = portStateBuilder.build();
            unicastGoalState.getGoalStateBuilder().putPortStates(portState.getConfiguration().getId(), portState);

            Goalstate.ResourceIdType portResourceId = Goalstate.ResourceIdType.newBuilder().setType(Common.ResourceType.PORT).setId(portState.getConfiguration().getId()).build();
            Goalstate.HostResources.Builder hostResourceBuilder = Goalstate.HostResources.newBuilder();
            hostResourceBuilder.addResources(portResourceId);
            unicastGoalState.getGoalStateBuilder().putHostResources(unicastGoalState.getHostIp(), hostResourceBuilder.build());
        }
    }
}
