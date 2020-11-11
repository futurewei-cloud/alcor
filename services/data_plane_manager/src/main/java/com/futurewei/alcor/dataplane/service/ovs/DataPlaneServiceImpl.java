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
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.dataplane.service.DataPlaneService;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Common.EtherType;
import com.futurewei.alcor.schema.Common.NetworkType;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.schema.Common.Protocol;
import com.futurewei.alcor.schema.DHCP.DHCPConfiguration;
import com.futurewei.alcor.schema.DHCP.DHCPState;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus;
import com.futurewei.alcor.schema.Neighbor.NeighborConfiguration;
import com.futurewei.alcor.schema.Neighbor.NeighborState;
import com.futurewei.alcor.schema.Neighbor.NeighborType;
import com.futurewei.alcor.schema.Port.PortConfiguration;
import com.futurewei.alcor.schema.Port.PortConfiguration.AllowAddressPair;
import com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import com.futurewei.alcor.schema.Port.PortConfiguration.SecurityGroupId;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.schema.Router.RouterConfiguration;
import com.futurewei.alcor.schema.Router.RouterState;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Direction;
import com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState;
import com.futurewei.alcor.schema.Subnet.SubnetConfiguration;
import com.futurewei.alcor.schema.Subnet.SubnetConfiguration.Gateway;
import com.futurewei.alcor.schema.Subnet.SubnetState;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration.SubnetId;
import com.futurewei.alcor.schema.Vpc.VpcState;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private void buildVpcStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (PortState portState: portStates) {
            VpcEntity vpcEntity = getVpcEntity(networkConfig, portState.getConfiguration().getVpcId());
            VpcConfiguration.Builder vpcConfigBuilder = VpcConfiguration.newBuilder();
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
                            SubnetId.Builder subnetIdBuilder = SubnetId.newBuilder();
                            subnetIdBuilder.setId(id);
                            vpcConfigBuilder.addSubnetIds(subnetIdBuilder.build());
                        });
            }
            //set routes here

            VpcState.Builder vpcStateBuilder = VpcState.newBuilder();
            vpcStateBuilder.setOperationType(networkConfig.getOpType());
            vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

            unicastGoalState.getGoalStateBuilder().addVpcStates(vpcStateBuilder.build());
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

    private void buildSubnetStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
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

            if (subnetEntity.getDhcpEnable() != null) {
                subnetConfigBuilder.setDhcpEnable(subnetEntity.getDhcpEnable());
            }

            if (subnetEntity.getAvailabilityZone() != null) {
                subnetConfigBuilder.setAvailabilityZone(subnetEntity.getAvailabilityZone());
            }

            if (subnetEntity.getPrimaryDns() != null) {
                subnetConfigBuilder.setPrimaryDns(subnetEntity.getPrimaryDns());
            }

            if (subnetEntity.getSecondaryDns() != null) {
                subnetConfigBuilder.setSecondaryDns(subnetEntity.getSecondaryDns());
            }

            SubnetState.Builder subnetStateBuilder = SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(networkConfig.getOpType());
            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
        }
    }

    private void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities,
                                UnicastGoalState unicastGoalState) {
        for (InternalPortEntity portEntity: portEntities) {
            PortConfiguration.Builder portConfigBuilder = PortConfiguration.newBuilder();
            portConfigBuilder.setId(portEntity.getId());
            portConfigBuilder.setMessageType(Common.MessageType.FULL);
            portConfigBuilder.setNetworkType(NetworkType.VXLAN);
            portConfigBuilder.setProjectId(portEntity.getProjectId());
            portConfigBuilder.setVpcId(portEntity.getVpcId());

            if (portEntity.getName() != null) {
                portConfigBuilder.setName(portEntity.getName());
            }

            portConfigBuilder.setMacAddress(portEntity.getMacAddress());
            portConfigBuilder.setAdminStateUp(portEntity.isAdminStateUp());

            HostInfo.Builder hostInfoBuilder = HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //TODO: Do we need mac address?
            //hostInfoBuilder.setMacAddress()
            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
            if (portEntity.getFixedIps() != null) {
                portEntity.getFixedIps().forEach(fixedIp -> {
                    FixedIp.Builder fixedIpBuilder = FixedIp.newBuilder();
                    fixedIpBuilder.setSubnetId(fixedIp.getSubnetId());
                    fixedIpBuilder.setIpAddress(fixedIp.getIpAddress());
                    portConfigBuilder.addFixedIps(fixedIpBuilder.build());
                });
            }

            if (portEntity.getAllowedAddressPairs() != null) {
                portEntity.getAllowedAddressPairs().forEach(pair -> {
                    AllowAddressPair.Builder allowAddressPairBuilder = AllowAddressPair.newBuilder();
                    allowAddressPairBuilder.setIpAddress(pair.getIpAddress());
                    allowAddressPairBuilder.setMacAddress(pair.getMacAddress());
                    portConfigBuilder.addAllowAddressPairs(allowAddressPairBuilder.build());
                });
            }

            if (portEntity.getSecurityGroups() != null) {
                portEntity.getSecurityGroups().forEach(securityGroupId-> {
                    SecurityGroupId.Builder securityGroupIdBuilder = SecurityGroupId.newBuilder();
                    securityGroupIdBuilder.setId(securityGroupId);
                    portConfigBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
                });
            }

            //PortState
            PortState.Builder portStateBuilder = PortState.newBuilder();
            portStateBuilder.setOperationType(networkConfig.getOpType());
            portStateBuilder.setConfiguration(portConfigBuilder.build());
            unicastGoalState.getGoalStateBuilder().addPortStates(portStateBuilder.build());
        }
    }

    private NeighborState buildNeighborState(NeighborEntry neighborEntry, NeighborInfo neighborInfo, OperationType operationType) {
        NeighborConfiguration.Builder neighborConfigBuilder = NeighborConfiguration.newBuilder();
        //neighborConfigBuilder.setId();
        //neighborConfigBuilder.setProjectId();
        neighborConfigBuilder.setVpcId(neighborInfo.getVpcId());
        //neighborConfigBuilder.setName();
        neighborConfigBuilder.setMacAddress(neighborInfo.getPortMac());
        neighborConfigBuilder.setHostIpAddress(neighborInfo.getHostIp());
        NeighborType neighborType = NeighborType.valueOf(neighborEntry.getNeighborType().getType());

        //TODO:setNeighborHostDvrMac
        //neighborConfigBuilder.setNeighborHostDvrMac();
        NeighborConfiguration.FixedIp.Builder fixedIpBuilder = NeighborConfiguration.FixedIp.newBuilder();
        fixedIpBuilder.setSubnetId(neighborInfo.getSubnetId());
        fixedIpBuilder.setIpAddress(neighborInfo.getPortIp());
        fixedIpBuilder.setNeighborType(neighborType);
        neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
        //TODO:setAllowAddressPairs
        //neighborConfigBuilder.setAllowAddressPairs();

        NeighborState.Builder neighborStateBuilder = NeighborState.newBuilder();
        neighborStateBuilder.setOperationType(operationType);
        neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());

        return neighborStateBuilder.build();
    }

    private void buildNeighborStates(NetworkConfiguration networkConfig, String hostIp,
                                     UnicastGoalState unicastGoalState,
                                     MulticastGoalState multicastGoalState) throws Exception {
        Map<String, NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }

        Map<String, List<NeighborEntry>> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<NeighborEntry> multicastNeighborEntries = new ArrayList<>();
        for (PortState portState: portStates) {
            List<FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            if (fixedIps == null) {
                throw new PortFixedIpNotFound();
            }

            for (FixedIp fixedIp: fixedIps) {
                List<NeighborEntry> neighborEntries = neighborTable.get(fixedIp.getIpAddress());
                if (neighborEntries == null) {
                    throw new NeighborInfoNotFound();
                }

                for (NeighborEntry neighborEntry: neighborEntries) {
                    NeighborInfo neighborInfo = neighborInfos.get(neighborEntry.getNeighborIp());
                    if (neighborInfo == null) {
                        throw new NeighborInfoNotFound();
                    }

                    if (hostIp.equals(neighborInfo.getHostIp())) {
                        continue;
                    }

                    unicastGoalState.getGoalStateBuilder().addNeighborStates(buildNeighborState(
                            neighborEntry, neighborInfo, networkConfig.getOpType()));
                }

                multicastNeighborEntries.addAll(neighborEntries);
            }
        }

        Set<NeighborInfo> neighborInfoSet = new HashSet<>();
        for (NeighborEntry neighborEntry: multicastNeighborEntries) {
            String localIp = neighborEntry.getLocalIp();
            String neighborIp = neighborEntry.getNeighborIp();
            NeighborInfo neighborInfo1 = neighborInfos.get(localIp);
            NeighborInfo neighborInfo2 = neighborInfos.get(neighborIp);
            if (neighborInfo1 == null || neighborInfo2 == null) {
                throw new NeighborInfoNotFound();
            }

            if (!multicastGoalState.getHostIps().contains(neighborInfo2.getHostIp())) {
                multicastGoalState.getHostIps().add(neighborInfo2.getHostIp());
            }

            if (!neighborInfoSet.contains(neighborInfo1)) {
                multicastGoalState.getGoalStateBuilder().addNeighborStates(buildNeighborState(
                        neighborEntry, neighborInfo1, networkConfig.getOpType()));
                neighborInfoSet.add(neighborInfo1);
            }
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


    private void buildSecurityGroupStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> securityGroupIds = new HashSet<>();
        for (PortState portState: portStates) {
            List<SecurityGroupId> securityGroupIdList= portState.getConfiguration().getSecurityGroupIdsList();
            if (securityGroupIdList != null && securityGroupIdList.size() >0) {
                securityGroupIds.addAll(securityGroupIdList.stream()
                        .map(SecurityGroupId::getId)
                        .collect(Collectors.toList()));
            }
        }

        for (String securityGroupId: securityGroupIds) {
            SecurityGroup securityGroup = getSecurityGroup(networkConfig, securityGroupId);
            SecurityGroupConfiguration.Builder securityGroupConfigBuilder = SecurityGroupConfiguration.newBuilder();
            securityGroupConfigBuilder.setId(securityGroup.getId());
            securityGroupConfigBuilder.setProjectId(securityGroup.getProjectId());
            //securityGroupConfigBuilder.setVpcId();
            securityGroupConfigBuilder.setName(securityGroup.getName());

            if (securityGroup.getSecurityGroupRules() == null) {
                throw new SecurityGroupRuleNotFound();
            }

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
            unicastGoalState.getGoalStateBuilder().addSecurityGroupStates(securityGroupStateBuilder.build());
        }
    }

    private void buildDhcpStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
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
                unicastGoalState.getGoalStateBuilder().addDhcpStates(dhcpStateBuilder.build());
            }
        }
    }

    private void buildRouterStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
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
        unicastGoalState.getGoalStateBuilder().addRouterStates(routerStateBuilder.build());

    }

    private UnicastGoalState buildUnicastGoalState(NetworkConfiguration networkConfig, String hostIp,
                                                   List<InternalPortEntity> portEntities,
                                                   MulticastGoalState multicastGoalState) throws Exception {
        UnicastGoalState unicastGoalState = new UnicastGoalState();
        unicastGoalState.setHostIp(hostIp);

        if (portEntities != null && portEntities.size() > 0) {
            buildPortState(networkConfig, portEntities, unicastGoalState);
        }

        buildVpcStates(networkConfig, unicastGoalState);
        buildSubnetStates(networkConfig, unicastGoalState);
        buildNeighborStates(networkConfig, hostIp, unicastGoalState, multicastGoalState);
        buildSecurityGroupStates(networkConfig, unicastGoalState);
        buildDhcpStates(networkConfig, unicastGoalState);
        buildRouterStates(networkConfig, unicastGoalState);

        unicastGoalState.setGoalState(unicastGoalState.getGoalStateBuilder().build());
        unicastGoalState.setGoalStateBuilder(null);

        return unicastGoalState;
    }

    private List<Map<String, List<GoalStateOperationStatus>>> createPortConfiguration(NetworkConfiguration networkConfig) throws Exception {
        List<UnicastGoalState> unicastGoalStates = new ArrayList<>();
        MulticastGoalState multicastGoalState = new MulticastGoalState();

        Map<String, List<InternalPortEntity>> hostPortEntities = new HashMap<>();
        for (InternalPortEntity portEntity: networkConfig.getPortEntities()) {
            if (portEntity.getBindingHostIP() == null) {
                throw new PortBindingHostIpNotFound();
            }

            if (!hostPortEntities.containsKey(portEntity.getBindingHostIP())) {
                hostPortEntities.put(portEntity.getBindingHostIP(), new ArrayList<>());
            }

            hostPortEntities.get(portEntity.getBindingHostIP()).add(portEntity);
        }

        for (Map.Entry<String, List<InternalPortEntity>> entry: hostPortEntities.entrySet()) {
            String hostIp = entry.getKey();
            List<InternalPortEntity> portEntities = entry.getValue();
            unicastGoalStates.add(buildUnicastGoalState(
                    networkConfig, hostIp, portEntities, multicastGoalState));
        }

        multicastGoalState.setGoalState(multicastGoalState.getGoalStateBuilder().build());
        multicastGoalState.setGoalStateBuilder(null);

        return dataPlaneClient.createGoalStates(unicastGoalStates, multicastGoalState);
    }

    private List<Map<String, List<GoalStateOperationStatus>>> createNeighborConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    private List<Map<String, List<GoalStateOperationStatus>>> createSecurityGroupConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    private List<Map<String, List<GoalStateOperationStatus>>> createRouterConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    @Override
    public InternalDPMResultList createNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        InternalDPMResultList resultAll = new InternalDPMResultList();
        long startTime = System.currentTimeMillis();

        List<Map<String, List<GoalStateOperationStatus>>> statuses;

        switch (networkConfig.getRsType()) {
            case PORT:
                statuses = createPortConfiguration(networkConfig);
                break;
            case NEIGHBOR:
                statuses = createNeighborConfiguration(networkConfig);
                break;
            case SECURITYGROUP:
                statuses = createSecurityGroupConfiguration(networkConfig);
                break;
            case ROUTER:
                statuses = createRouterConfiguration(networkConfig);
                break;
            default:
                throw new UnknownResourceType();
        }

        AtomicInteger failed = new AtomicInteger(0);
        resultAll.setOverrallTime(System.currentTimeMillis() - startTime);
        List<InternalDPMResult> result = statuses.stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .map(status -> {
                    if (status == null) {
                        failed.incrementAndGet();
                        return null;
                    }

                    return new InternalDPMResult(status.getResourceId(), status.getResourceType().toString(),
                            status.getOperationStatus().toString(), status.getStateElapseTime());
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        resultAll.setResultList(result);
        if (failed.get() == 0) {
            resultAll.setResultMessage("Successfully Handle request !!");
        } else {
            resultAll.setResultMessage("Failed Handle request !!");
        }

        return resultAll;
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
