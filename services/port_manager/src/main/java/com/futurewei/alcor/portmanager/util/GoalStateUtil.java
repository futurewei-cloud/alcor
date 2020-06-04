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
package com.futurewei.alcor.portmanager.util;

import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.schema.Vpc.*;
import com.futurewei.alcor.schema.Common.*;
import com.futurewei.alcor.schema.Port.*;
import com.futurewei.alcor.schema.SecurityGroup.*;
import com.futurewei.alcor.schema.Goalstate.*;
import com.futurewei.alcor.schema.Subnet.*;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.port.FixedIp;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;

public class GoalStateUtil {
    public static VpcState buildVpcState(VpcEntity vpcEntity, OperationType operationType) {
        VpcConfiguration.Builder vpcConfigBuilder = VpcConfiguration.newBuilder();

        //Required fields
        vpcConfigBuilder.setId(vpcEntity.getId());

        //Optional fields
        if (vpcEntity.getProjectId() != null) {
            vpcConfigBuilder.setProjectId(vpcEntity.getProjectId());
        }

        if (vpcEntity.getName() != null) {
            vpcConfigBuilder.setName(vpcEntity.getName());
        }

        if (vpcEntity.getCidr() != null) {
            vpcConfigBuilder.setCidr(vpcEntity.getCidr());
        }

        if (vpcEntity.getTenantId() != null) {
            vpcConfigBuilder.setTunnelId(Long.parseLong(vpcEntity.getTenantId()));
        }

        VpcState.Builder vpcStateBuilder = VpcState.newBuilder();
        vpcStateBuilder.setOperationType(operationType);
        vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

        return vpcStateBuilder.build();
    }

    public static SubnetState buildSubnetState(SubnetEntity subnet, OperationType operationType) {
        SubnetConfiguration.Builder subnetConfigBuilder = SubnetConfiguration.newBuilder();

        //Required fields
        subnetConfigBuilder.setId(subnet.getId());
        subnetConfigBuilder.setVpcId(subnet.getVpcId());
        subnetConfigBuilder.setCidr(subnet.getCidr());

        SubnetConfiguration.Gateway.Builder gatewayBuilder =
                SubnetConfiguration.Gateway.newBuilder()
                        .setIpAddress(subnet.getGatewayIp())
                        .setMacAddress(subnet.getGatewayMacAddress());

        subnetConfigBuilder.setGateway(gatewayBuilder.build());

        //Optional fields
        if (subnet.getName() != null) {
            subnetConfigBuilder.setName(subnet.getName());
        }

        if (subnet.getProjectId() != null) {
            subnetConfigBuilder.setProjectId(subnet.getProjectId());
        }

        if (subnet.getAvailabilityZone() != null) {
            subnetConfigBuilder.setAvailabilityZone(subnet.getAvailabilityZone());
        }

        if (subnet.getDhcpEnable() != null) {
            subnetConfigBuilder.setDhcpEnable(subnet.getDhcpEnable());
        }

        if (subnet.getPrimaryDns() != null) {
            subnetConfigBuilder.setPrimaryDns(subnet.getPrimaryDns());
        }

        if (subnet.getSecondaryDns() != null) {
            subnetConfigBuilder.setSecondaryDns(subnet.getSecondaryDns());
        }

        SubnetState.Builder subnetStateBuilder = SubnetState.newBuilder();
        subnetStateBuilder.setOperationType(operationType);
        subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());

        return subnetStateBuilder.build();
    }

    private static int DirectionToNumber(String direction) {
        if (direction.equals("egress")) {
            return 0;
        } else if (direction.equals("ingress")) {
            return 1;
        }

        return -1;
    }

    private static int etherTypeToNumber(String etherType) {
        if (etherType.equals("IPv4")) {
            return 0;
        } else if (etherType.equals("IPv6")) {
            return 1;
        }

        return -1;
    }

    private static int protocolToNumber(String protocol) {
        switch(protocol) {
            case "tcp":
                return 0;
            case "udp":
                return 1;
            case "icmp":
                return 2;
            case "http":
                return 3;
            case "arp":
                return 4;
            default:
                return -1;
        }
    }

    public static SecurityGroupState buildSecurityGroupState(SecurityGroup securityGroup, OperationType operationType) {
        SecurityGroupConfiguration.Builder securityGroupConfigBuilder = SecurityGroupConfiguration.newBuilder();

        //Required fields
        securityGroupConfigBuilder.setId(securityGroup.getId());

        //Optional fields
        if (securityGroup.getName() != null) {
            securityGroupConfigBuilder.setName(securityGroup.getName());
        }

        if (securityGroup.getProjectId() != null) {
            securityGroupConfigBuilder.setProjectId(securityGroup.getProjectId());
        }

        for (SecurityGroupRule securityGroupRule: securityGroup.getSecurityGroupRules()) {
            SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder =
                    SecurityGroupConfiguration.SecurityGroupRule.newBuilder();

            //Required fields
            securityGroupRuleBuilder.setId(securityGroupRule.getId());
            securityGroupRuleBuilder.setDirection(SecurityGroupConfiguration.Direction
                    .forNumber(DirectionToNumber(securityGroupRule.getDirection())));

            //Optional fields
            if (securityGroupRule.getEtherType() != null) {
                securityGroupRuleBuilder.setEthertype(EtherType
                        .forNumber(etherTypeToNumber(securityGroupRule.getEtherType())));
            }

            if (securityGroupRule.getProtocol() != null) {
                securityGroupRuleBuilder.setProtocol(Protocol
                        .forNumber(protocolToNumber(securityGroupRule.getProtocol())));
            }

            if (securityGroupRule.getPortRangeMax() != null) {
                securityGroupRuleBuilder.setPortRangeMax(securityGroupRule.getPortRangeMax());
            }

            if (securityGroupRule.getPortRangeMin() != null) {
                securityGroupRuleBuilder.setPortRangeMin(securityGroupRule.getPortRangeMin());
            }

            if (securityGroupRule.getRemoteGroupId() != null) {
                securityGroupRuleBuilder.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
            }

            if (securityGroupRule.getRemoteIpPrefix() != null) {
                securityGroupRuleBuilder.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
            }

            if (securityGroupRule.getSecurityGroupId() != null) {
                securityGroupRuleBuilder.setSecurityGroupId(securityGroupRule.getSecurityGroupId());
            }

            securityGroupConfigBuilder.addSecurityGroupRules(securityGroupRuleBuilder.build());
        }

        SecurityGroupState.Builder securityGroupStateBuilder = SecurityGroupState.newBuilder();
        securityGroupStateBuilder.setOperationType(operationType);
        securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());

        return securityGroupStateBuilder.build();
    }

    public static PortState buildPortState(PortEntity portEntity, Map<String, NodeInfo> nodeInfoMap, OperationType operationType) {
        PortConfiguration.Builder portConfigBuilder = PortConfiguration.newBuilder();

        //Required fields
        portConfigBuilder.setId(portEntity.getId());
        portConfigBuilder.setVpcId(portEntity.getVpcId());
        portConfigBuilder.setMacAddress(portEntity.getMacAddress());

        for (FixedIp fixedIp: portEntity.getFixedIps()) {
            Port.PortConfiguration.FixedIp.Builder fixedIpsBuilder =
                    Port.PortConfiguration.FixedIp.newBuilder();

            fixedIpsBuilder.setSubnetId(fixedIp.getSubnetId());
            fixedIpsBuilder.setIpAddress(fixedIp.getIpAddress());
            portConfigBuilder.addFixedIps(fixedIpsBuilder.build());
        }

        if (portEntity.getBindingHostId() != null) {
            PortConfiguration.HostInfo.Builder hostInfoBuilder = PortConfiguration.HostInfo.newBuilder();
            NodeInfo nodeInfo = nodeInfoMap.get(portEntity.getBindingHostId());
            hostInfoBuilder.setIpAddress(nodeInfo.getLocalIp());
            hostInfoBuilder.setMacAddress(nodeInfo.getMacAddress());

            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
        }

        //Optional fields
        portConfigBuilder.setAdminStateUp(portEntity.isAdminStateUp());

        if (portEntity.getName() != null) {
            portConfigBuilder.setName(portEntity.getName());
        }

        if (portEntity.getVethName() != null) {
            portConfigBuilder.setVethName(portEntity.getVethName());
        }

        if (portEntity.getProjectId() != null) {
            portConfigBuilder.setProjectId(portEntity.getProjectId());
        }

        PortState.Builder portStateBuilder = PortState.newBuilder();
        portStateBuilder.setOperationType(operationType);
        portStateBuilder.setConfiguration(portConfigBuilder.build());

        return portStateBuilder.build();
    }

    public static GoalState buildGoalState(List<Object> entities, OperationType operationType) throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        Map<String, NodeInfo> nodeInfoMap  = new HashMap<>();
        Map<String, VpcEntity> vpcEntityMap = new HashMap<>();
        Map<String, SubnetEntity> subnetEntityMap = new HashMap<>();
        Map<String, SecurityGroup> securityGroupMap = new HashMap<>();

        for (Object entity: entities) {
            if (entity instanceof VpcEntity) {
                VpcEntity vpcEntity = (VpcEntity)entity;
                vpcEntityMap.put(vpcEntity.getId(), (VpcEntity)entity);
            } else if (entity instanceof SubnetEntity) {
                SubnetEntity subnetEntity = (SubnetEntity) entity;
                subnetEntityMap.put(subnetEntity.getId(), subnetEntity);
            } else if (entity instanceof SecurityGroup) {
                SecurityGroup securityGroupEntity = (SecurityGroup) entity;
                securityGroupMap.put(securityGroupEntity.getId(), securityGroupEntity);
            } else if (entity instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) entity;
                nodeInfoMap.put(nodeInfo.getId(), nodeInfo);
            } else if (entity instanceof PortEntity) {
                portEntities.add((PortEntity)entity);
            }
        }

        GoalState.Builder goalStateBuilder = GoalState.newBuilder();

        //Make sure we add These common states once
        Set<String> vpcStates = new HashSet<>();
        Set<String> subnetStates = new HashSet<>();
        Set<String> securityGroupStates = new HashSet<>();

        for (PortEntity portEntity: portEntities) {
            String bindingHostId = portEntity.getBindingHostId();

            //Make sure we can get NodeInfo before building PortState
            if (bindingHostId != null && nodeInfoMap.get(bindingHostId) == null) {
                throw new NodeInfoNotFound();
            }

            //Build VpcState
            VpcEntity vpcEntity = vpcEntityMap.get(portEntity.getVpcId());
            if (vpcEntity == null) {
                throw new VpcEntityNotFound();
            }

            if (!vpcStates.contains(portEntity.getVpcId())) {
                VpcState vpcState = buildVpcState(vpcEntity, operationType);
                goalStateBuilder.addVpcStates(vpcState);
                vpcStates.add(portEntity.getVpcId());
            }

            //Build SubnetState
            for (FixedIp fixedIp: portEntity.getFixedIps()) {
                String subnetId = fixedIp.getSubnetId();
                SubnetEntity subnetEntity = subnetEntityMap.get(subnetId);
                if (subnetEntity == null) {
                    throw new SubnetEntityNotFound();
                }

                if (!subnetStates.contains(subnetId)) {
                    SubnetState subnetState = buildSubnetState(subnetEntity, operationType);
                    goalStateBuilder.addSubnetStates(subnetState);
                    subnetStates.add(subnetId);
                }
            }

            //Build SecurityGroupState
            if (portEntity.getSecurityGroups() != null) {
                for (String securityGroupId: portEntity.getSecurityGroups()) {
                    SecurityGroup securityGroup = securityGroupMap.get(securityGroupId);
                    if (securityGroup == null) {
                        throw new SecurityGroupEntityNotFound();
                    }

                    if (!securityGroupStates.contains(securityGroupId)) {
                        SecurityGroupState securityGroupState =
                                buildSecurityGroupState(securityGroup, operationType);
                        goalStateBuilder.addSecurityGroupStates(securityGroupState);
                        securityGroupStates.add(securityGroupId);
                    }
                }
            } else {
                SecurityGroup securityGroup = null;
                for (Map.Entry<String, SecurityGroup> entry: securityGroupMap.entrySet()) {
                    if ("default".equals(entry.getValue().getName())) {
                        securityGroup = entry.getValue();
                    }
                }

                if (securityGroup == null) {
                    throw new DefaultSecurityGroupEntityNotFound();
                }

                if (!securityGroupStates.contains(securityGroup.getId())) {
                    SecurityGroupState securityGroupState =
                            buildSecurityGroupState(securityGroup, operationType);
                    goalStateBuilder.addSecurityGroupStates(securityGroupState);
                    securityGroupStates.add(securityGroup.getId());
                }
            }

            //Build PortState
            PortState portState = buildPortState(portEntity, nodeInfoMap, operationType);
            goalStateBuilder.addPortStates(portState);
        }

        return goalStateBuilder.build();
    }
}
