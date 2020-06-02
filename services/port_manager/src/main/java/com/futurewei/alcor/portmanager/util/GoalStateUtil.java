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
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;

public class GoalStateUtil {
    public static Vpc.VpcState buildVpcState(VpcEntity vpcEntity, Common.OperationType operationType) {
        Vpc.VpcConfiguration.Builder vpcConfigBuilder = Vpc.VpcConfiguration.newBuilder();

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

        Vpc.VpcState.Builder vpcStateBuilder = Vpc.VpcState.newBuilder();
        vpcStateBuilder.setOperationType(operationType);
        vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

        return vpcStateBuilder.build();
    }

    public static Subnet.SubnetState buildSubnetState(SubnetEntity subnet, Common.OperationType operationType) {
        Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();

        //Required fields
        subnetConfigBuilder.setId(subnet.getId());
        subnetConfigBuilder.setVpcId(subnet.getVpcId());
        subnetConfigBuilder.setCidr(subnet.getCidr());

        Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder =
                Subnet.SubnetConfiguration.Gateway.newBuilder()
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

        Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
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

    public static SecurityGroup.SecurityGroupState buildSecurityGroupState(SecurityGroupEntity securityGroupEntity, Common.OperationType operationType) {
        SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigBuilder = SecurityGroup.SecurityGroupConfiguration.newBuilder();

        //Required fields
        securityGroupConfigBuilder.setId(securityGroupEntity.getId());

        //Optional fields
        if (securityGroupEntity.getName() != null) {
            securityGroupConfigBuilder.setName(securityGroupEntity.getName());
        }

        if (securityGroupEntity.getProjectId() != null) {
            securityGroupConfigBuilder.setProjectId(securityGroupEntity.getProjectId());
        }

        for (SecurityGroupRuleEntity securityGroupRule: securityGroupEntity.getSecurityGroupRuleEntities()) {
            SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder =
                    SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.newBuilder();

            //Required fields
            securityGroupRuleBuilder.setId(securityGroupRule.getId());
            securityGroupRuleBuilder.setDirection(SecurityGroup.SecurityGroupConfiguration.Direction
                    .forNumber(DirectionToNumber(securityGroupRule.getDirection())));

            //Optional fields
            if (securityGroupRule.getEtherType() != null) {
                securityGroupRuleBuilder.setEthertype(Common.EtherType
                        .forNumber(etherTypeToNumber(securityGroupRule.getEtherType())));
            }

            if (securityGroupRule.getProtocol() != null) {
                securityGroupRuleBuilder.setProtocol(Common.Protocol
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

        SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder =
                SecurityGroup.SecurityGroupState.newBuilder();
        securityGroupStateBuilder.setOperationType(operationType);
        securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());

        return securityGroupStateBuilder.build();
    }

    public static Port.PortState buildPortState(PortEntity portEntity, Map<String, NodeInfo> nodeInfoMap, Common.OperationType operationType) {
        Port.PortConfiguration.Builder portConfigBuilder = Port.PortConfiguration.newBuilder();

        //Required fields
        portConfigBuilder.setId(portEntity.getId());
        portConfigBuilder.setNetworkId(portEntity.getNetworkId());
        portConfigBuilder.setMacAddress(portEntity.getMacAddress());

        for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
            Port.PortConfiguration.FixedIp.Builder fixedIpsBuilder =
                    Port.PortConfiguration.FixedIp.newBuilder();

            fixedIpsBuilder.setSubnetId(fixedIp.getSubnetId());
            fixedIpsBuilder.setIpAddress(fixedIp.getIpAddress());
            portConfigBuilder.addFixedIps(fixedIpsBuilder.build());
        }

        Port.PortConfiguration.HostInfo.Builder hostInfoBuilder =
                Port.PortConfiguration.HostInfo.newBuilder();
        NodeInfo nodeInfo = nodeInfoMap.get(portEntity.getBindingHostId());
        hostInfoBuilder.setIpAddress(nodeInfo.getLocalIp());
        hostInfoBuilder.setMacAddress(nodeInfo.getMacAddress());

        portConfigBuilder.setHostInfo(hostInfoBuilder.build());

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

        Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
        portStateBuilder.setOperationType(operationType);
        portStateBuilder.setConfiguration(portConfigBuilder.build());

        return portStateBuilder.build();
    }

    public static Goalstate.GoalState buildGoalState(List<Object> entities, Common.OperationType operationType) throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        Map<String, NodeInfo> nodeInfoMap  = new HashMap<>();
        Map<String, VpcEntity> vpcEntityMap = new HashMap<>();
        Map<String, SubnetEntity> subnetEntityMap = new HashMap<>();
        Map<String, SecurityGroupEntity> securityGroupEntityMap = new HashMap<>();

        for (Object entity: entities) {
            if (entity instanceof VpcEntity) {
                VpcEntity vpcEntity = (VpcEntity)entity;
                vpcEntityMap.put(vpcEntity.getId(), (VpcEntity)entity);
            } else if (entity instanceof SubnetEntity) {
                SubnetEntity subnetEntity = (SubnetEntity) entity;
                subnetEntityMap.put(subnetEntity.getId(), subnetEntity);
            } else if (entity instanceof SecurityGroupEntity) {
                SecurityGroupEntity securityGroupEntity = (SecurityGroupEntity) entity;
                securityGroupEntityMap.put(securityGroupEntity.getId(), securityGroupEntity);
            } else if (entity instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) entity;
                nodeInfoMap.put(nodeInfo.getId(), nodeInfo);
            } else if (entity instanceof PortEntity) {
                portEntities.add((PortEntity)entity);
            }
        }

        Goalstate.GoalState.Builder goalStateBuilder = Goalstate.GoalState.newBuilder();

        //Make sure we add These common states once
        Set<String> vpcStates = new HashSet<>();
        Set<String> subnetStates = new HashSet<>();
        Set<String> securityGroupStates = new HashSet<>();

        for (PortEntity portEntity: portEntities) {
            String bindingHostId = portEntity.getBindingHostId();
            if (bindingHostId == null) {
                continue;
            }

            //Make sure we can get NodeInfo before building PortState
            if (nodeInfoMap.get(bindingHostId) == null) {
                throw new NodeInfoNotFound();
            }

            //Build VpcState
            VpcEntity vpcEntity = vpcEntityMap.get(portEntity.getNetworkId());
            if (vpcEntity == null) {
                throw new VpcEntityNotFound();
            }

            if (!vpcStates.contains(portEntity.getNetworkId())) {
                Vpc.VpcState vpcState = buildVpcState(vpcEntity, operationType);
                goalStateBuilder.addVpcStates(vpcState);
                vpcStates.add(portEntity.getNetworkId());
            }

            //Build SubnetState
            for (PortEntity.FixedIp fixedIp: portEntity.getFixedIps()) {
                String subnetId = fixedIp.getSubnetId();
                SubnetEntity subnetEntity = subnetEntityMap.get(subnetId);
                if (subnetEntity == null) {
                    throw new SubnetEntityNotFound();
                }

                if (!subnetStates.contains(subnetId)) {
                    Subnet.SubnetState subnetState = buildSubnetState(subnetEntity, operationType);
                    goalStateBuilder.addSubnetStates(subnetState);
                    subnetStates.add(subnetId);
                }
            }

            //Build SecurityGroupState
            if (portEntity.getSecurityGroups() != null) {
                for (String securityGroupId: portEntity.getSecurityGroups()) {
                    SecurityGroupEntity securityGroupEntity = securityGroupEntityMap.get(securityGroupId);
                    if (securityGroupEntity == null) {
                        throw new SecurityGroupEntityNotFound();
                    }

                    if (!securityGroupStates.contains(securityGroupId)) {
                        SecurityGroup.SecurityGroupState securityGroupState =
                                buildSecurityGroupState(securityGroupEntity, operationType);
                        goalStateBuilder.addSecurityGroupStates(securityGroupState);
                        securityGroupStates.add(securityGroupId);
                    }
                }
            } else {
                SecurityGroupEntity securityGroupEntity = null;
                for (Map.Entry<String, SecurityGroupEntity> entry: securityGroupEntityMap.entrySet()) {
                    if ("default".equals(entry.getValue().getName())) {
                        securityGroupEntity = entry.getValue();
                    }
                }

                if (securityGroupEntity == null) {
                    throw new DefaultSecurityGroupEntityNotFound();
                }

                if (!securityGroupStates.contains(securityGroupEntity.getId())) {
                    SecurityGroup.SecurityGroupState securityGroupState =
                            buildSecurityGroupState(securityGroupEntity, operationType);
                    goalStateBuilder.addSecurityGroupStates(securityGroupState);
                    securityGroupStates.add(securityGroupEntity.getId());
                }
            }

            //Build PortState
            Port.PortState portState = buildPortState(portEntity, nodeInfoMap, operationType);
            goalStateBuilder.addPortStates(portState);
        }

        return goalStateBuilder.build();
    }
}
