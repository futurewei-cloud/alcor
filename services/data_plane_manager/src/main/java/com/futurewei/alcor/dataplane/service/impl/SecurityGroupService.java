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
import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.schema.Common.EtherType;
import com.futurewei.alcor.schema.Common.Protocol;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Direction;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private Direction transformDirection(String direction) throws Exception {
        if (StringUtils.isEmpty(direction)) {
            throw new SecurityGroupDirectionInvalid();
        }

        switch (direction) {
            case "ingress":
                return Direction.INGRESS;
            case "egress":
                return Direction.EGRESS;
        }

        throw new SecurityGroupDirectionInvalid();
    }

    private EtherType transformEtherType(String etherType) throws Exception {
        if (StringUtils.isEmpty(etherType)) {
            return EtherType.IPV4;
        }

        switch (etherType) {
            case "IPv4":
                return EtherType.IPV4;
            case "IPv6":
                return EtherType.IPV6;
        }

        throw new SecurityGroupEtherTypeInvalid();
    }

    private Protocol transformProtocol(String protocol) throws Exception {
        if (StringUtils.isEmpty(protocol)) {
            throw new SecurityGroupProtocolInvalid();
        }

        switch (protocol) {
            case "tcp":
                return Protocol.TCP;
            case "udp":
                return Protocol.UDP;
            case "icmp":
                return Protocol.ICMP;
            case "http":
                return Protocol.HTTP;
        }

        throw new SecurityGroupProtocolInvalid();
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
                securityGroupRuleBuilder.setDirection(transformDirection(securityGroupRule.getDirection()));
                securityGroupRuleBuilder.setEthertype(transformEtherType(securityGroupRule.getEtherType()));

                if (securityGroupRule.getProtocol() != null) {
                    securityGroupRuleBuilder.setProtocol(transformProtocol(securityGroupRule.getProtocol()));
                }

                if (securityGroupRule.getPortRangeMin() != null) {
                    securityGroupRuleBuilder.setPortRangeMin(securityGroupRule.getPortRangeMin());
                }

                if (securityGroupRule.getPortRangeMax() != null) {
                    securityGroupRuleBuilder.setPortRangeMax(securityGroupRule.getPortRangeMax());
                }

                if (securityGroupRule.getRemoteIpPrefix() != null) {
                    securityGroupRuleBuilder.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
                }

                if (securityGroupRule.getRemoteGroupId() != null) {
                    securityGroupRuleBuilder.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
                }

                securityGroupConfigBuilder.addSecurityGroupRules(securityGroupRuleBuilder.build());
            }

            com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.newBuilder();
            securityGroupStateBuilder.setOperationType(networkConfig.getOpType());
            securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addSecurityGroupStates(securityGroupStateBuilder.build());
        }
    }
}
