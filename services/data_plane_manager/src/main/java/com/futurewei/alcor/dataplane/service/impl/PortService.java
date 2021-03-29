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
import com.futurewei.alcor.schema.Common;
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
}
