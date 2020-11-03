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

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.HostGoalState;
import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.dataplane.service.DataPlaneService;
import com.futurewei.alcor.dataplane.utils.DataPlaneManagerValidationUtil;
import com.futurewei.alcor.schema.Common.MessageType;
import com.futurewei.alcor.schema.Common.NetworkType;
import com.futurewei.alcor.schema.Common.EtherType;
import com.futurewei.alcor.schema.Common.Protocol;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.schema.DHCP.DHCPState;
import com.futurewei.alcor.schema.DHCP.DHCPConfiguration;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Neighbor.NeighborState;
import com.futurewei.alcor.schema.Neighbor.NeighborConfiguration;
import com.futurewei.alcor.schema.Neighbor.NeighborType;
import com.futurewei.alcor.schema.Port.PortConfiguration;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;
import com.futurewei.alcor.schema.Port.PortConfiguration.AllowAddressPair;
import com.futurewei.alcor.schema.Port.PortConfiguration.SecurityGroupId;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.schema.Router.RouterState;
import com.futurewei.alcor.schema.Router.RouterConfiguration;
import com.futurewei.alcor.schema.Subnet.SubnetConfiguration;
import com.futurewei.alcor.schema.Subnet.SubnetConfiguration.Gateway;
import com.futurewei.alcor.schema.Subnet.SubnetState;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration.SubnetId;
import com.futurewei.alcor.schema.Vpc.VpcState;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Direction;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataPlaneServiceImpl implements DataPlaneService {
    @Autowired
    private DataPlaneClient dataPlaneClient;

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

    private void buildVpcState(NetworkConfiguration networkConfig, GoalState.Builder goalStateBuilder) throws Exception {
        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (PortState portState: portStates) {
            VpcEntity vpcEntity = getVpcEntity(networkConfig, portState.getConfiguration().getVpcId());
            VpcConfiguration.Builder vpcConfigBuilder = VpcConfiguration.newBuilder();
            vpcConfigBuilder.setId(vpcEntity.getId());
            vpcConfigBuilder.setProjectId(vpcEntity.getProjectId());
            vpcConfigBuilder.setName(vpcEntity.getName());
            vpcConfigBuilder.setCidr(vpcEntity.getCidr());
            vpcConfigBuilder.setTunnelId(Long.parseLong(vpcEntity.getSegmentationId() + ""));

            networkConfig.getSubnets().stream()
                    .filter(s -> s.getVpcId().equals(vpcEntity.getId()))
                    .map(InternalSubnetEntity::getId)
                    .forEach(id -> {
                        SubnetId.Builder subnetIdBuilder = SubnetId.newBuilder();
                        subnetIdBuilder.setId(id);
                        vpcConfigBuilder.addSubnetIds(subnetIdBuilder.build());
                    });

            //set routes here

            VpcState.Builder vpcStateBuilder = VpcState.newBuilder();
            vpcStateBuilder.setOperationType(networkConfig.getOpType());
            vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

            goalStateBuilder.addVpcStates(vpcStateBuilder.build());
        }
    }

    private InternalSubnetEntity getInternalSubnetEntity(NetworkConfiguration networkConfig, String subnetId) throws Exception {
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

    private void buildSubnetState(NetworkConfiguration networkConfig, GoalState.Builder goalStateBuilder) throws Exception {
        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        for (PortState portState: portStates) {
            for (FixedIp fixedIp: portState.getConfiguration().getFixedIpsList()) {
                InternalSubnetEntity internalSubnetEntity = getInternalSubnetEntity(
                        networkConfig, fixedIp.getSubnetId());
                subnetEntities.add(internalSubnetEntity);
            }
        }

        for (InternalSubnetEntity subnetEntity: subnetEntities) {
            SubnetConfiguration.Builder subnetConfigBuilder = SubnetConfiguration.newBuilder();
            subnetConfigBuilder.setId(subnetEntity.getId());
            subnetConfigBuilder.setNetworkType(NetworkType.VXLAN);
            subnetConfigBuilder.setProjectId(subnetEntity.getProjectId());
            subnetConfigBuilder.setVpcId(subnetEntity.getVpcId());
            subnetConfigBuilder.setName(subnetEntity.getName());
            subnetConfigBuilder.setCidr(subnetEntity.getCidr());
            subnetConfigBuilder.setTunnelId(subnetEntity.getTunnelId());

            Gateway.Builder gatewayBuilder = Gateway.newBuilder();
            gatewayBuilder.setIpAddress(subnetEntity.getGatewayIp());
            gatewayBuilder.setMacAddress(subnetEntity.getGatewayMacAddress());
            subnetConfigBuilder.setGateway(gatewayBuilder.build());
            subnetConfigBuilder.setDhcpEnable(subnetEntity.getDhcpEnable());
            subnetConfigBuilder.setAvailabilityZone(subnetEntity.getAvailabilityZone());
            if (subnetEntity.getPrimaryDns() != null) {
                subnetConfigBuilder.setPrimaryDns(subnetEntity.getPrimaryDns());
            }

            if (subnetEntity.getSecondaryDns() != null) {
                subnetConfigBuilder.setSecondaryDns(subnetEntity.getSecondaryDns());
            }

            SubnetState.Builder subnetStateBuilder = SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(networkConfig.getOpType());
            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            goalStateBuilder.addSubnetStates(subnetStateBuilder.build());
        }
    }

    private void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities, GoalState.Builder goalStateBuilder) {
        for (InternalPortEntity portEntity: portEntities) {
            PortConfiguration.Builder portConfigBuilder = PortConfiguration.newBuilder();
            portConfigBuilder.setId(portEntity.getId());
            portConfigBuilder.setMessageType(MessageType.FULL);
            portConfigBuilder.setNetworkType(NetworkType.VXLAN);
            portConfigBuilder.setProjectId(portEntity.getProjectId());
            portConfigBuilder.setVpcId(portEntity.getVpcId());
            portConfigBuilder.setName(portEntity.getName());
            portConfigBuilder.setMacAddress(portEntity.getMacAddress());
            portConfigBuilder.setAdminStateUp(portEntity.isAdminStateUp());

            HostInfo.Builder hostInfoBuilder = HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //TODO: Do we need mac address?
            //hostInfoBuilder.setMacAddress()
            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
            portEntity.getFixedIps().forEach(fixedIp -> {
                FixedIp.Builder fixedIpBuilder = FixedIp.newBuilder();
                fixedIpBuilder.setSubnetId(fixedIp.getSubnetId());
                fixedIpBuilder.setIpAddress(fixedIp.getIpAddress());
                portConfigBuilder.addFixedIps(fixedIpBuilder.build());
            });

            portEntity.getAllowedAddressPairs().forEach(pair -> {
                AllowAddressPair.Builder allowAddressPairBuilder = AllowAddressPair.newBuilder();
                allowAddressPairBuilder.setIpAddress(pair.getIpAddress());
                allowAddressPairBuilder.setMacAddress(pair.getMacAddress());
                portConfigBuilder.addAllowAddressPairs(allowAddressPairBuilder.build());
            });

            portEntity.getSecurityGroups().forEach(securityGroupId-> {
                SecurityGroupId.Builder securityGroupIdBuilder = SecurityGroupId.newBuilder();
                securityGroupIdBuilder.setId(securityGroupId);
                portConfigBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
            });

            //PortState
            PortState.Builder portStateBuilder = PortState.newBuilder();
            portStateBuilder.setOperationType(networkConfig.getOpType());
            portStateBuilder.setConfiguration(portConfigBuilder.build());
            goalStateBuilder.addPortStates(portStateBuilder.build());
        }
    }

    private NeighborInfo getNeighborInfo(NetworkConfiguration networkConfig, String hostIp) throws Exception {
        NeighborInfo result = null;
        for (NeighborInfo neighborInfo: networkConfig.getNeighborInfos()) {
            if (neighborInfo.getHostId().equals(hostIp)) {
                result = neighborInfo;
                break;
            }
        }

        if (result == null) {
            throw new NeighborInfoNotFound();
        }

        return result;
    }

    private void buildNeighborState(NetworkConfiguration networkConfig, String hostIp, GoalState.Builder goalStateBuilder) throws Exception {
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }

        List<NeighborEntry> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (NeighborEntry neighborEntry: neighborTable) {
            if (!hostIp.equals(neighborEntry.getLocalIp())) {
                continue;
            }

            NeighborInfo neighborInfo = getNeighborInfo(networkConfig, neighborEntry.getNeighborIp());
            NeighborConfiguration.Builder neighborConfigBuilder = NeighborConfiguration.newBuilder();
            //neighborConfigBuilder.setId();
            NeighborType neighborType = NeighborType.valueOf(neighborEntry.getNeighborType().getType());
            //neighborConfigBuilder.setNeighborType(neighborType);
            //neighborConfigBuilder.setProjectId();
            neighborConfigBuilder.setVpcId(neighborInfo.getVpcId());
            //neighborConfigBuilder.setName();
            neighborConfigBuilder.setMacAddress(neighborInfo.getPortMac());
            neighborConfigBuilder.setHostIpAddress(neighborInfo.getHostIp());
            //TODO:setNeighborHostDvrMac
            //neighborConfigBuilder.setNeighborHostDvrMac();
            NeighborConfiguration.FixedIp.Builder fixedIpBuilder = NeighborConfiguration.FixedIp.newBuilder();
            fixedIpBuilder.setSubnetId(neighborInfo.getSubnetId());
            fixedIpBuilder.setIpAddress(neighborInfo.getPortIp());
            neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
            //TODO:setAllowAddressPairs
            //neighborConfigBuilder.setAllowAddressPairs();

            NeighborState.Builder neighborStateBuilder = NeighborState.newBuilder();
            neighborStateBuilder.setOperationType(networkConfig.getOpType());
            neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());
            goalStateBuilder.addNeighborStates(neighborStateBuilder.build());
        }
    }

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


    private void buildSecurityGroupState(NetworkConfiguration networkConfig, GoalState.Builder goalStateBuilder) throws Exception {
        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> securityGroupIds = new HashSet<>();
        for (PortState portState: portStates) {
            List<SecurityGroupId> securityGroupIdList= portState.getConfiguration().getSecurityGroupIdsList();
            securityGroupIds.addAll(securityGroupIdList.stream()
                    .map(SecurityGroupId::getId)
                    .collect(Collectors.toList()));
        }

        for (String securityGroupId: securityGroupIds) {
            SecurityGroup securityGroup = getSecurityGroup(networkConfig, securityGroupId);
            SecurityGroupConfiguration.Builder securityGroupConfigBuilder = SecurityGroupConfiguration.newBuilder();
            securityGroupConfigBuilder.setId(securityGroup.getId());
            securityGroupConfigBuilder.setProjectId(securityGroup.getProjectId());
            //securityGroupConfigBuilder.setVpcId();
            securityGroupConfigBuilder.setName(securityGroup.getName());

            for (SecurityGroupRule securityGroupRule: securityGroup.getSecurityGroupRules()) {
                SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder =
                        SecurityGroupConfiguration.SecurityGroupRule.newBuilder();
                securityGroupRuleBuilder.setSecurityGroupId(securityGroup.getId());
                securityGroupRuleBuilder.setId(securityGroupRule.getId());
                securityGroupRuleBuilder.setDirection(Direction.valueOf(securityGroupRule.getDirection()));
                securityGroupRuleBuilder.setEthertype(EtherType.valueOf(securityGroupRule.getEtherType()));
                securityGroupRuleBuilder.setProtocol(Protocol.valueOf(securityGroupRule.getProtocol()));
                securityGroupRuleBuilder.setPortRangeMin(securityGroupRule.getPortRangeMin());
                securityGroupRuleBuilder.setPortRangeMax(securityGroupRule.getPortRangeMax());
                securityGroupRuleBuilder.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
                securityGroupRuleBuilder.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
                securityGroupConfigBuilder.addSecurityGroupRules(securityGroupRuleBuilder.build());
            }

            SecurityGroupState.Builder securityGroupStateBuilder = SecurityGroupState.newBuilder();
            securityGroupStateBuilder.setOperationType(networkConfig.getOpType());
            securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());
            goalStateBuilder.addSecurityGroupStates(securityGroupStateBuilder.build());
        }
    }

    private void buildDhcpState(NetworkConfiguration networkConfig, GoalState.Builder goalStateBuilder) throws Exception {
        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (PortState portState: portStates) {
            String macAddress = portState.getConfiguration().getMacAddress();
            List<FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (FixedIp fixedIp: fixedIps) {
                DHCPConfiguration.Builder dhcpConfigBuilder = DHCPConfiguration.newBuilder();
                dhcpConfigBuilder.setMacAddress(macAddress);
                dhcpConfigBuilder.setIpv4Address(fixedIp.getIpAddress());
                //TODO: support ipv6
                //dhcpConfigBuilder.setIpv6Address();
                //dhcpConfigBuilder.setPortHostName();
                //dhcpConfigBuilder.setExtraDhcpOptions();
                //dhcpConfigBuilder.setDnsEntryList();

                DHCPState.Builder dhcpStateBuilder = DHCPState.newBuilder();
                dhcpStateBuilder.setOperationType(networkConfig.getOpType());
                dhcpStateBuilder.setConfiguration(dhcpConfigBuilder.build());
                goalStateBuilder.addDhcpStates(dhcpStateBuilder.build());
            }
        }
    }

    private void buildRouterState(NetworkConfiguration networkConfig, GoalState.Builder goalStateBuilder) throws Exception {
        List<PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> subnetIds = new HashSet<>();
        for (PortState portState: portStates) {
            List<FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (FixedIp fixedIp: fixedIps) {
                InternalSubnetEntity internalSubnetEntity =
                        getInternalSubnetEntity(networkConfig, fixedIp.getSubnetId());
                subnetIds.add(internalSubnetEntity.getId());
            }
        }

        RouterConfiguration.Builder routerConfigBuilder = RouterConfiguration.newBuilder();
        //routerConfigBuilder.setHostDvrMacAddress();
        //routerConfigBuilder.addAllSubnetIds(subnetIds);

        RouterState.Builder routerStateBuilder = RouterState.newBuilder();
        routerStateBuilder.setOperationType(networkConfig.getOpType());
        routerStateBuilder.setConfiguration(routerConfigBuilder.build());
        goalStateBuilder.addRouterStates(routerStateBuilder.build());

    }

    private HostGoalState buildHostGoalState(NetworkConfiguration networkConfig, String hostIp, List<InternalPortEntity> portEntities) throws Exception {
        GoalState.Builder goalStateBuilder = GoalState.newBuilder();

        if (portEntities != null && portEntities.size() > 0) {
            buildPortState(networkConfig, portEntities, goalStateBuilder);
        }

        buildVpcState(networkConfig, goalStateBuilder);
        buildSubnetState(networkConfig, goalStateBuilder);
        buildNeighborState(networkConfig, hostIp, goalStateBuilder);
        buildSecurityGroupState(networkConfig, goalStateBuilder);
        buildDhcpState(networkConfig, goalStateBuilder);
        buildRouterState(networkConfig, goalStateBuilder);

        HostGoalState hostGoalState = new HostGoalState();
        hostGoalState.setHostIp(hostIp);
        hostGoalState.setGoalState(goalStateBuilder.build());

        return hostGoalState;
    }

    @Override
    public InternalDPMResultList createNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {

        // validation for networkConfig
        DataPlaneManagerValidationUtil.validateInput(networkConfig);
        List<HostGoalState> hostGoalStates = new ArrayList<>();
        if (ResourceType.PORT.equals(networkConfig.getRsType())) {
            Map<String, List<InternalPortEntity>> hostPortEntities = new HashMap<>();
            for (InternalPortEntity portEntity: networkConfig.getPortEntities()) {
                if (!hostPortEntities.containsKey(portEntity.getBindingHostIP())) {
                    hostPortEntities.put(portEntity.getBindingHostIP(), new ArrayList<>());
                }
                hostPortEntities.get(portEntity.getBindingHostIP()).add(portEntity);
            }

            for (Map.Entry<String, List<InternalPortEntity>> entry: hostPortEntities.entrySet()) {
                String hostIp = entry.getKey();
                List<InternalPortEntity> portEntities = entry.getValue();
                hostGoalStates.add(buildHostGoalState(networkConfig, hostIp, portEntities));
            }
        } else if (ResourceType.NEIGHBOR.equals(networkConfig.getRsType())) {
            //hostGoalStates.add(buildHostGoalState(networkConfig, null, null));
        } else if (ResourceType.SECURITYGROUP.equals(networkConfig.getRsType())) {
            //hostGoalStates.add(buildHostGoalState(networkConfig, null, null));
        } else if (ResourceType.ROUTER.equals(networkConfig.getRsType())) {
            //hostGoalStates.add(buildHostGoalState(networkConfig, null, null));
        } else {
            throw new UnknownResourceType();
        }

        dataPlaneClient.createGoalState(hostGoalStates);

        return null;
    }

    @Override
    public InternalDPMResultList updateNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception {
        return null;
    }

    @Override
    public InternalDPMResultList deleteNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception {
        return null;
    }
}
