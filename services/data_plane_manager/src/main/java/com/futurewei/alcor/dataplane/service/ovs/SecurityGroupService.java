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
import com.futurewei.alcor.dataplane.exception.SecurityGroupNotFound;
import com.futurewei.alcor.dataplane.exception.SecurityGroupRuleNotFound;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SecurityGroupService extends ResourceService {
    private SecurityGroup getSecurityGroup(NetworkConfiguration networkConfig, String securityGroupId) throws Exception {
        SecurityGroup result = null;
        for (SecurityGroup securityGroup: networkConfig.getSecurityGroups()) {
            if (securityGroup.getId().equals(securityGroupId)) {
                result = securityGroup;
                break;
            }
        }

        if (result == null) {
            throw new SecurityGroupNotFound();
        }

        return result;
    }


    public void buildSecurityGroupStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> securityGroupIds = new HashSet<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.SecurityGroupId> securityGroupIdList= portState.getConfiguration().getSecurityGroupIdsList();
            if (securityGroupIdList != null && securityGroupIdList.size() >0) {
                securityGroupIds.addAll(securityGroupIdList.stream()
                        .map(Port.PortConfiguration.SecurityGroupId::getId)
                        .collect(Collectors.toList()));
            }
        }

        for (String securityGroupId: securityGroupIds) {
            SecurityGroup securityGroup = getSecurityGroup(networkConfig, securityGroupId);
            com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.newBuilder();
            securityGroupConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
            securityGroupConfigBuilder.setId(securityGroup.getId());
            //securityGroupConfigBuilder.setVpcId();
            securityGroupConfigBuilder.setName(securityGroup.getName());

            if (securityGroup.getSecurityGroupRules() == null) {
                throw new SecurityGroupRuleNotFound();
            }

            for (SecurityGroupRule securityGroupRule: securityGroup.getSecurityGroupRules()) {
                com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder =
                        com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.newBuilder();
                securityGroupRuleBuilder.setSecurityGroupId(securityGroup.getId());
                securityGroupRuleBuilder.setId(securityGroupRule.getId());
                securityGroupRuleBuilder.setDirection(com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Direction.valueOf(securityGroupRule.getDirection()));
                securityGroupRuleBuilder.setEthertype(Common.EtherType.valueOf(securityGroupRule.getEtherType()));
                securityGroupRuleBuilder.setProtocol(Common.Protocol.valueOf(securityGroupRule.getProtocol()));
                securityGroupRuleBuilder.setPortRangeMin(securityGroupRule.getPortRangeMin());
                securityGroupRuleBuilder.setPortRangeMax(securityGroupRule.getPortRangeMax());
                securityGroupRuleBuilder.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
                securityGroupRuleBuilder.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
                securityGroupConfigBuilder.addSecurityGroupRules(securityGroupRuleBuilder.build());
            }

            com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.newBuilder();
            securityGroupStateBuilder.setOperationType(networkConfig.getOpType());
            securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addSecurityGroupStates(securityGroupStateBuilder.build());
        }
    }
}
