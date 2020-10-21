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
package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.dataplane.constants.DPMAutoUnitTestConstant;
import com.futurewei.alcor.dataplane.entity.*;
import com.futurewei.alcor.dataplane.exception.NeighborInfoNotFound;
import com.futurewei.alcor.dataplane.exception.SecurityGroupNotFound;
import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.dataplane.exception.VpcEntityNotFound;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.schema.Router;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;
import java.util.stream.Collectors;

public class DataPlaneManagerUtil {

    /**
     * Automatically generate the input of UTs, that is, the contractor of Port Manager to DPM
     * @param operationType
     * @param resourceType
     * @param portNumPerHost
     * @param hostNum
     * @param subnetNum
     * @param NumOfIPsInSubnet1
     * @param NumOfIPsInSubnet2
     * @param hasInternalRouterInfo
     * @param hasInternalSubnetRoutingTable
     * @param hasInternalRoutingRule
     * @param hasNeighbor
     * @param neighborNum
     * @param fastPath
     * @return
     */
    public NetworkConfiguration autoGenerateUTsInput(int operationType,
                                                     int resourceType,
                                                     int portNumPerHost,
                                                     int hostNum,
                                                     int subnetNum,
                                                     int NumOfIPsInSubnet1,
                                                     int NumOfIPsInSubnet2,
                                                     boolean hasInternalRouterInfo,
                                                     boolean hasInternalSubnetRoutingTable,
                                                     boolean hasInternalRoutingRule,
                                                     boolean hasNeighbor,
                                                     int neighborNum,
                                                     boolean fastPath) {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        // set operationType and resourceType
        networkConfiguration.setOpType(Common.OperationType.forNumber(operationType));
        networkConfiguration.setRsType(Common.ResourceType.forNumber(resourceType));

        // set routers_internal
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();
        if (hasInternalRouterInfo) {
            InternalRouterInfo routerInfo = new InternalRouterInfo();
            InternalRouterConfiguration internalRouterConfiguration = new InternalRouterConfiguration();
            List<InternalSubnetRoutingTable> subnet_routing_tables = new ArrayList<>();

            if (hasInternalSubnetRoutingTable) {
                InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
                List<InternalRoutingRule> routing_rules = new ArrayList<>();

                if (hasInternalRoutingRule){
                    InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                    routing_rules.add(internalRoutingRule);
                }

                internalSubnetRoutingTable.setRoutingRules(routing_rules);
                subnet_routing_tables.add(internalSubnetRoutingTable);
            }

            internalRouterConfiguration.setSubnetRoutingTables(subnet_routing_tables);


            //routerInfo.setOperationType(OperationType.valueOf("create"));
            routerInfo.setRouterConfiguration(internalRouterConfiguration);
            internalRouterInfos.add(routerInfo);
        }

        networkConfiguration.setInternalRouterInfos(internalRouterInfos);

        // set neighborInfos
        List<NeighborInfo> neighborINFO = new ArrayList<>();
        if (hasNeighbor) {
            for (int i = 0; i < neighborNum; i ++) {
                NeighborInfo neighborInfo = new NeighborInfo(DPMAutoUnitTestConstant.hostIp + i, DPMAutoUnitTestConstant.hostId, DPMAutoUnitTestConstant.portId + i, DPMAutoUnitTestConstant.portMac, DPMAutoUnitTestConstant.portIp, DPMAutoUnitTestConstant.vpcId, DPMAutoUnitTestConstant.subnetId + 0);
                neighborINFO.add(neighborInfo);
            }
        }
        networkConfiguration.setNeighborInfos(neighborINFO);

        // set neighborTable
        List<NeighborEntry> neighborTable = new ArrayList<>();
        for (int i = 0; i < NumOfIPsInSubnet1; i ++) {
            int IpAddressOffset = i + 2;
            for (int j = i + 1; j < NumOfIPsInSubnet1; j ++) {
                int NeighborIpAddressOffset = j + 2;
                NeighborEntry neighborEntry = new NeighborEntry();
                neighborEntry.setLocalIp(DPMAutoUnitTestConstant.L2localIp + IpAddressOffset);
                neighborEntry.setNeighborIp(DPMAutoUnitTestConstant.L2neighborIp + NeighborIpAddressOffset);
                neighborEntry.setNeighborType(NeighborEntry.NeighborType.L2);
                neighborTable.add(neighborEntry);
            }
        }

        for (int i = 0; i < NumOfIPsInSubnet2; i ++) {
            int IpAddressOffset = i + 2;
            for (int j = i + 1; j < NumOfIPsInSubnet2; j ++) {
                int NeighborIpAddressOffset = j + 2;
                NeighborEntry neighborEntry = new NeighborEntry();
                neighborEntry.setLocalIp(DPMAutoUnitTestConstant.L3localIp + IpAddressOffset);
                neighborEntry.setNeighborIp(DPMAutoUnitTestConstant.L3neighborIp + NeighborIpAddressOffset);
                neighborEntry.setNeighborType(NeighborEntry.NeighborType.L2);
                neighborTable.add(neighborEntry);
            }
        }

        for (int i = 0; i < NumOfIPsInSubnet1; i ++) {
            int IpAddressOffset = i + 2;
            for (int j = 0; j < NumOfIPsInSubnet2; j ++) {
                int NeighborIpAddressOffset = j + 2;
                NeighborEntry neighborEntry = new NeighborEntry();
                neighborEntry.setLocalIp(DPMAutoUnitTestConstant.L2localIp + IpAddressOffset);
                neighborEntry.setNeighborIp(DPMAutoUnitTestConstant.L3neighborIp + NeighborIpAddressOffset);
                neighborEntry.setNeighborType(NeighborEntry.NeighborType.L3);
                neighborTable.add(neighborEntry);
            }
        }
        networkConfiguration.setNeighborTable(neighborTable);

        // set portEntities
        List<InternalPortEntity> portEntities = new ArrayList<>();
        for (int j = 0; j < hostNum; j ++) {
            for (int i = 0; i < portNumPerHost; i ++) {
                List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
                PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(DPMAutoUnitTestConstant.subnetId + i, DPMAutoUnitTestConstant.IpAddress + i);
                fixedIps.add(fixedIp);

                PortEntity portEntity = new PortEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.portId + i,
                        DPMAutoUnitTestConstant.portName + i, "", DPMAutoUnitTestConstant.vpcId, true, DPMAutoUnitTestConstant.portMacAddress + i, DPMAutoUnitTestConstant.vethName + i, fastPath,
                        null, null, null, fixedIps, null, null, null,
                        DPMAutoUnitTestConstant.bindingHostId, null, null, null, null,
                        DPMAutoUnitTestConstant.networkNamespace, null, null, null, null, null,
                        null, false, null, null, 0, null, null,
                        false, false);

                List<RouteEntity> routeEntities = new ArrayList<>();
                if (hasInternalRouterInfo) {
                    RouteEntity routeEntity = new RouteEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.routeId + i,
                            DPMAutoUnitTestConstant.routeName, "", DPMAutoUnitTestConstant.destination, DPMAutoUnitTestConstant.target, 0, RouteTableType.VPC, "");
                    routeEntities.add(routeEntity);
                }

                String bindingHostIP = DPMAutoUnitTestConstant.bindingHostIp + j;

                InternalPortEntity port = new InternalPortEntity(portEntity, routeEntities, bindingHostIP);
                portEntities.add(port);
            }
        }

        networkConfiguration.setPortEntities(portEntities);

        // set vpcs
        List<VpcEntity> vpcs = new ArrayList<>();
        VpcEntity vpc = new VpcEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.vpcId, DPMAutoUnitTestConstant.vpcName,
                "", null, false, null, null, false, null,
                null, null, false, null, false, false, false,
                null, null, null, null, null, null, null,
                null, null, null, null, null, DPMAutoUnitTestConstant.cidr);
        vpcs.add(vpc);
        networkConfiguration.setVpcs(vpcs);

        // set subnets
        List<InternalSubnetEntity> subnets = new ArrayList<>();
        for (int i = 0; i < subnetNum; i ++) {
            int IpAddressOffSet = i + 2;
            SubnetEntity subnetEntity = new SubnetEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.subnetId + i, DPMAutoUnitTestConstant.subnetName + i, "", DPMAutoUnitTestConstant.vpcId,
                    "192.168." + IpAddressOffSet + ".0/24", DPMAutoUnitTestConstant.availabilityZone, "192.168." + IpAddressOffSet + ".1", false, null,
                    null, null, DPMAutoUnitTestConstant.gatewayMacAddress, null,
                    null, null, null, null, null,
                    null, null, false, null, null,
                    null, false, null, null,
                    null, null, null, null, null,
                    null, null, false, null, null,
                    null);
            Long tunnelId = Long.parseLong(DPMAutoUnitTestConstant.tunnelId + i);
            InternalSubnetEntity subnet = new InternalSubnetEntity(subnetEntity, tunnelId);
            subnets.add(subnet);
        }
        networkConfiguration.setSubnets(subnets);

        // set securityGroups
        List<SecurityGroup> securityGroups = new ArrayList<>();
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroups.add(securityGroup);
        networkConfiguration.setSecurityGroups(securityGroups);

        return networkConfiguration;
    }

    /**
     * Automatically generate the output of UTs, that is, the contractor of DPM to ACA
     * @param operationType
     * @param resourceType
     * @param portNumPerHost
     * @param hostNum
     * @param subnetNum
     * @param NumOfIPsInSubnet1
     * @param NumOfIPsInSubnet2
     * @param hasInternalRouterInfo
     * @param hasInternalSubnetRoutingTable
     * @param hasInternalRoutingRule
     * @param hasNeighbor
     * @param neighborNum
     * @param fastPath
     * @return
     * @throws Exception
     */
    public Map<String, Goalstate.GoalState> autoGenerateUTsOutput(int operationType,
                                                                  int resourceType,
                                                                  int portNumPerHost,
                                                                  int hostNum,
                                                                  int subnetNum,
                                                                  int NumOfIPsInSubnet1,
                                                                  int NumOfIPsInSubnet2,
                                                                  boolean hasInternalRouterInfo,
                                                                  boolean hasInternalSubnetRoutingTable,
                                                                  boolean hasInternalRoutingRule,
                                                                  boolean hasNeighbor,
                                                                  int neighborNum,
                                                                  boolean fastPath) throws Exception {
        Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
        for (int i = 0; i < hostNum; i ++) {
            HostGoalState hostGoalState = new HostGoalState();
            NetworkConfiguration networkConfiguration = autoGenerateUTsInput(operationType, resourceType, portNumPerHost, hostNum, subnetNum, NumOfIPsInSubnet1, NumOfIPsInSubnet2, hasInternalRouterInfo, hasInternalSubnetRoutingTable, hasInternalRoutingRule, hasNeighbor, neighborNum, fastPath);
            hostGoalState = buildHostGoalState(networkConfiguration, DPMAutoUnitTestConstant.hostIp + i, networkConfiguration.getPortEntities());
            goalStateHashMap.put(hostGoalState.getHostIp(), hostGoalState.getGoalState());
        }

        return goalStateHashMap;
    }

    /**
     * get VpcEntity from NetworkConfiguration
     * @param networkConfig
     * @param vpcId
     * @return
     * @throws Exception
     */
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

    /**
     * build VpcState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildVpcState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (Port.PortState portState: portStates) {
            VpcEntity vpcEntity = getVpcEntity(networkConfig, portState.getConfiguration().getVpcId());
            Vpc.VpcConfiguration.Builder vpcConfigBuilder = Vpc.VpcConfiguration.newBuilder();
            vpcConfigBuilder.setId(vpcEntity.getId());
            vpcConfigBuilder.setProjectId(vpcEntity.getProjectId());
            vpcConfigBuilder.setName(vpcEntity.getName());
            vpcConfigBuilder.setCidr(vpcEntity.getCidr());
            //vpcConfigBuilder.setTunnelId(Long.parseLong());

            networkConfig.getSubnets().stream()
                    .filter(s -> s.getVpcId().equals(vpcEntity.getId()))
                    .map(InternalSubnetEntity::getId)
                    .forEach(id -> {
                        Vpc.VpcConfiguration.SubnetId.Builder subnetIdBuilder = Vpc.VpcConfiguration.SubnetId.newBuilder();
                        subnetIdBuilder.setId(id);
                        vpcConfigBuilder.addSubnetIds(subnetIdBuilder.build());
                    });

            //set routes here

            Vpc.VpcState.Builder vpcStateBuilder = Vpc.VpcState.newBuilder();
            vpcStateBuilder.setOperationType(networkConfig.getOpType());
            vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

            goalStateBuilder.addVpcStates(vpcStateBuilder.build());
        }
    }

    /**
     * get InternalSubnetEntity from NetworkConfiguration
     * @param networkConfig
     * @param subnetId
     * @return
     * @throws Exception
     */
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

    /**
     * build SubnetState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildSubnetState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        List<String> subnetIdsInFixedIP = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            for (Port.PortConfiguration.FixedIp fixedIp: portState.getConfiguration().getFixedIpsList()) {
                if (!subnetIdsInFixedIP.contains(fixedIp.getSubnetId())) {
                    InternalSubnetEntity internalSubnetEntity = getInternalSubnetEntity(
                            networkConfig, fixedIp.getSubnetId());
                    subnetEntities.add(internalSubnetEntity);
                    subnetIdsInFixedIP.add(fixedIp.getSubnetId());
                }
            }
        }

        for (InternalSubnetEntity subnetEntity: subnetEntities) {
            Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
            subnetConfigBuilder.setId(subnetEntity.getId());
            subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
            subnetConfigBuilder.setProjectId(subnetEntity.getProjectId());
            subnetConfigBuilder.setVpcId(subnetEntity.getVpcId());
            subnetConfigBuilder.setName(subnetEntity.getName());
            subnetConfigBuilder.setCidr(subnetEntity.getCidr());
            subnetConfigBuilder.setTunnelId(subnetEntity.getTunnelId());

            Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
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
            Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(networkConfig.getOpType());
            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            goalStateBuilder.addSubnetStates(subnetStateBuilder.build());
        }
    }

    /**
     * build PortState in GoalState
     * @param networkConfig
     * @param portEntities
     * @param goalStateBuilder
     */
    private void buildPortState(NetworkConfiguration networkConfig, List<InternalPortEntity> portEntities, Goalstate.GoalState.Builder goalStateBuilder) {
        for (InternalPortEntity portEntity: portEntities) {
            Port.PortConfiguration.Builder portConfigBuilder = Port.PortConfiguration.newBuilder();
            portConfigBuilder.setId(portEntity.getId());
            portConfigBuilder.setMessageType(Common.MessageType.FULL);
            portConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
            portConfigBuilder.setProjectId(portEntity.getProjectId());
            portConfigBuilder.setVpcId(portEntity.getVpcId());
            portConfigBuilder.setName(portEntity.getName());
            portConfigBuilder.setMacAddress(portEntity.getMacAddress());
            portConfigBuilder.setAdminStateUp(portEntity.isAdminStateUp());

            Port.PortConfiguration.HostInfo.Builder hostInfoBuilder = Port.PortConfiguration.HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //TODO: Do we need mac address?
            //hostInfoBuilder.setMacAddress()
            portConfigBuilder.setHostInfo(hostInfoBuilder.build());
            portEntity.getFixedIps().forEach(fixedIp -> {
                Port.PortConfiguration.FixedIp.Builder fixedIpBuilder = Port.PortConfiguration.FixedIp.newBuilder();
                fixedIpBuilder.setSubnetId(fixedIp.getSubnetId());
                fixedIpBuilder.setIpAddress(fixedIp.getIpAddress());
                portConfigBuilder.addFixedIps(fixedIpBuilder.build());
            });

            if (portEntity.getAllowedAddressPairs() != null) {
                portEntity.getAllowedAddressPairs().forEach(pair -> {
                    Port.PortConfiguration.AllowAddressPair.Builder allowAddressPairBuilder = Port.PortConfiguration.AllowAddressPair.newBuilder();
                    allowAddressPairBuilder.setIpAddress(pair.getIpAddress());
                    allowAddressPairBuilder.setMacAddress(pair.getMacAddress());
                    portConfigBuilder.addAllowAddressPairs(allowAddressPairBuilder.build());
                });
            }


            if (portEntity.getSecurityGroups() != null) {
                portEntity.getSecurityGroups().forEach(securityGroupId-> {
                    Port.PortConfiguration.SecurityGroupId.Builder securityGroupIdBuilder = Port.PortConfiguration.SecurityGroupId.newBuilder();
                    securityGroupIdBuilder.setId(securityGroupId);
                    portConfigBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
                });
            }

            //PortState
            Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
            portStateBuilder.setOperationType(networkConfig.getOpType());
            portStateBuilder.setConfiguration(portConfigBuilder.build());
            goalStateBuilder.addPortStates(portStateBuilder.build());
        }
    }

    /**
     * get NeighborInfo from NetworkConfiguration
     * @param networkConfig
     * @param hostIp
     * @return
     * @throws Exception
     */
    private NeighborInfo getNeighborInfo(NetworkConfiguration networkConfig, String hostIp) throws Exception {
        NeighborInfo result = null;
        for (NeighborInfo neighborInfo: networkConfig.getNeighborInfos()) {
            if (neighborInfo.getHostIp().equals(hostIp)) {
                result = neighborInfo;
                break;
            }
        }

        if (result == null) {
            throw new NeighborInfoNotFound();
        }

        return result;
    }

    /**
     * build NeighborState in GoalState
     * @param networkConfig
     * @param hostIp
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildNeighborState(NetworkConfiguration networkConfig, String hostIp, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }

        List<NeighborEntry> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (NeighborEntry neighborEntry: neighborTable) {

            NeighborInfo neighborInfo = getNeighborInfo(networkConfig, hostIp);
            if (!hostIp.equals(neighborInfo.getHostIp())) {
                continue;
            }
            Neighbor.NeighborConfiguration.Builder neighborConfigBuilder = Neighbor.NeighborConfiguration.newBuilder();
            //neighborConfigBuilder.setId();
            Neighbor.NeighborType neighborType = Neighbor.NeighborType.valueOf(neighborEntry.getNeighborType().getType());
            //neighborConfigBuilder.setNeighborType(neighborType);
            //neighborConfigBuilder.setProjectId();
            neighborConfigBuilder.setVpcId(neighborInfo.getVpcId());
            //neighborConfigBuilder.setName();
            neighborConfigBuilder.setMacAddress(neighborInfo.getPortMac());
            neighborConfigBuilder.setHostIpAddress(neighborInfo.getHostIp());
            //TODO:setNeighborHostDvrMac
            //neighborConfigBuilder.setNeighborHostDvrMac();
            Neighbor.NeighborConfiguration.FixedIp.Builder fixedIpBuilder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
            fixedIpBuilder.setSubnetId(neighborInfo.getSubnetId());
            fixedIpBuilder.setIpAddress(neighborInfo.getPortIp());
            neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
            //TODO:setAllowAddressPairs
            //neighborConfigBuilder.setAllowAddressPairs();

            Neighbor.NeighborState.Builder neighborStateBuilder = Neighbor.NeighborState.newBuilder();
            neighborStateBuilder.setOperationType(networkConfig.getOpType());
            neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());
            goalStateBuilder.addNeighborStates(neighborStateBuilder.build());
        }
    }

    /**
     * get SecurityGroup from NetworkConfiguration
     * @param networkConfig
     * @param securityGroupId
     * @return
     * @throws Exception
     */
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

    /**
     * build SecurityGroupState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildSecurityGroupState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> securityGroupIds = new HashSet<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.SecurityGroupId> securityGroupIdList= portState.getConfiguration().getSecurityGroupIdsList();
            securityGroupIds.addAll(securityGroupIdList.stream()
                    .map(Port.PortConfiguration.SecurityGroupId::getId)
                    .collect(Collectors.toList()));
        }

        for (String securityGroupId: securityGroupIds) {
            SecurityGroup securityGroup = getSecurityGroup(networkConfig, securityGroupId);
            com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.newBuilder();
            securityGroupConfigBuilder.setId(securityGroup.getId());
            securityGroupConfigBuilder.setProjectId(securityGroup.getProjectId());
            //securityGroupConfigBuilder.setVpcId();
            securityGroupConfigBuilder.setName(securityGroup.getName());

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
            goalStateBuilder.addSecurityGroupStates(securityGroupStateBuilder.build());
        }
    }

    /**
     * build DhcpState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildDhcpState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (Port.PortState portState: portStates) {
            String macAddress = portState.getConfiguration().getMacAddress();
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                DHCP.DHCPConfiguration.Builder dhcpConfigBuilder = DHCP.DHCPConfiguration.newBuilder();
                dhcpConfigBuilder.setMacAddress(macAddress);
                dhcpConfigBuilder.setIpv4Address(fixedIp.getIpAddress());
                //TODO: support ipv6
                //dhcpConfigBuilder.setIpv6Address();
                //dhcpConfigBuilder.setPortHostName();
                //dhcpConfigBuilder.setExtraDhcpOptions();
                //dhcpConfigBuilder.setDnsEntryList();

                DHCP.DHCPState.Builder dhcpStateBuilder = DHCP.DHCPState.newBuilder();
                dhcpStateBuilder.setOperationType(networkConfig.getOpType());
                dhcpStateBuilder.setConfiguration(dhcpConfigBuilder.build());
                goalStateBuilder.addDhcpStates(dhcpStateBuilder.build());
            }
        }
    }

    /**
     * build RouterState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildRouterState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> subnetIds = new HashSet<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                InternalSubnetEntity internalSubnetEntity =
                        getInternalSubnetEntity(networkConfig, fixedIp.getSubnetId());
                subnetIds.add(internalSubnetEntity.getId());
            }
        }

        Router.RouterConfiguration.Builder routerConfigBuilder = Router.RouterConfiguration.newBuilder();
        //routerConfigBuilder.setHostDvrMacAddress();
        //routerConfigBuilder.addAllSubnetIds(subnetIds);

        Router.RouterState.Builder routerStateBuilder = Router.RouterState.newBuilder();
        routerStateBuilder.setOperationType(networkConfig.getOpType());
        routerStateBuilder.setConfiguration(routerConfigBuilder.build());
        goalStateBuilder.addRouterStates(routerStateBuilder.build());

    }

    /**
     * build GoalState in a host
     * @param networkConfig
     * @param hostIp
     * @param portEntities
     * @return
     * @throws Exception
     */
    private HostGoalState buildHostGoalState(NetworkConfiguration networkConfig, String hostIp, List<InternalPortEntity> portEntities) throws Exception {
        Goalstate.GoalState.Builder goalStateBuilder = Goalstate.GoalState.newBuilder();

        if (portEntities != null && portEntities.size() > 0) {
            buildPortState(networkConfig, portEntities, goalStateBuilder);
        }

        //buildVpcState(networkConfig, goalStateBuilder);
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

    public NetworkConfiguration autoGenerateUTsInput_MoreCustomizableScenarios(int operationType,
                                                                               int resourceType,
                                                                               Map<String, List<UTPortWithSubnetAndIPMapping>> hostAndPortsMapping, // key - bindingHostIP
                                                                               List<UTSubnetInfo> UTSubnets,
                                                                               List<UTL3NeighborInfoMapping> L3NeighborInfoMapping,
                                                                               boolean hasInternalRouterInfo,
                                                                               boolean hasInternalSubnetRoutingTable,
                                                                               boolean hasInternalRoutingRule,
                                                                               boolean hasNeighbor,
                                                                               Map<String, UTNeighborInfoDetail> neighborInfoDetails, // key - port_IP
                                                                               boolean fastPath) {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        // set operationType and resourceType
        networkConfiguration.setOpType(Common.OperationType.forNumber(operationType));
        networkConfiguration.setRsType(Common.ResourceType.forNumber(resourceType));

        // set neighborInfos
        Map<String, NeighborInfo> neighborInfoMap = new HashMap<>(); // key - port_ip


        // set routers_internal
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();
        if (hasInternalRouterInfo) {
            InternalRouterInfo routerInfo = new InternalRouterInfo();
            InternalRouterConfiguration internalRouterConfiguration = new InternalRouterConfiguration();
            List<InternalSubnetRoutingTable> subnet_routing_tables = new ArrayList<>();

            if (hasInternalSubnetRoutingTable) {
                InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
                List<InternalRoutingRule> routing_rules = new ArrayList<>();

                if (hasInternalRoutingRule){
                    InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                    routing_rules.add(internalRoutingRule);
                }

                internalSubnetRoutingTable.setRoutingRules(routing_rules);
                subnet_routing_tables.add(internalSubnetRoutingTable);
            }

            internalRouterConfiguration.setSubnetRoutingTables(subnet_routing_tables);


            //routerInfo.setOperationType(OperationType.valueOf("create"));
            routerInfo.setRouterConfiguration(internalRouterConfiguration);
            internalRouterInfos.add(routerInfo);
        }

        networkConfiguration.setInternalRouterInfos(internalRouterInfos);




        // set neighborTable
        // configure L3
        List<NeighborEntry> neighborTable = new ArrayList<>();
        for (int i = 0; i < L3NeighborInfoMapping.size(); i ++) {
            UTL3NeighborInfoMapping local = L3NeighborInfoMapping.get(i);
            String subnetId = local.getSubnetId();
            List<UTIPInfo> localIPsInSubnet = local.getIPsInSubnet();
            for (int j = 0; j < localIPsInSubnet.size(); j ++) {
                String localIP = localIPsInSubnet.get(j).getIp();
                boolean isExist = localIPsInSubnet.get(j).isExist();
                if (isExist) {
                    NeighborInfo neighborInfo = new NeighborInfo();
                    UTNeighborInfoDetail detail = neighborInfoDetails.get(localIP);
                    neighborInfo.setPortIp(localIP);
                    neighborInfo.setVpcId(DPMAutoUnitTestConstant.vpcId);
                    neighborInfo.setSubnetId(subnetId);
                    neighborInfo.setPortId(detail.getPortId());
                    neighborInfo.setPortMac(detail.getPortMac());
                    neighborInfo.setHostId(detail.getHostId());
                    neighborInfo.setHostIp(detail.getHostIp());
                    neighborInfoMap.put(localIP, neighborInfo);
                    continue;
                }
                for (int k = i + 1; k < L3NeighborInfoMapping.size(); k ++) {
                    UTL3NeighborInfoMapping neighbor = L3NeighborInfoMapping.get(k);
                    List<UTIPInfo> neighborIPsInSubnet = neighbor.getIPsInSubnet();
                    for (int m = 0; m < neighborIPsInSubnet.size(); m ++) {
                        String neighborIP = neighborIPsInSubnet.get(m).getIp();
                        NeighborEntry neighborEntry = new NeighborEntry();
                        neighborEntry.setLocalIp(localIP);
                        neighborEntry.setNeighborIp(neighborIP);
                        neighborEntry.setNeighborType(NeighborEntry.NeighborType.L3);
                        neighborTable.add(neighborEntry);
                    }
                }
            }
        }

        // configure L2
        for (int i = 0; i < L3NeighborInfoMapping.size(); i ++) {
            UTL3NeighborInfoMapping local = L3NeighborInfoMapping.get(i);
            List<UTIPInfo> localIPsInSubnet = local.getIPsInSubnet();
            boolean[] isUsed = new boolean[L3NeighborInfoMapping.size() + 1];
            for (int j = 0; j < localIPsInSubnet.size(); j ++) {
                String localIP = localIPsInSubnet.get(j).getIp();
                boolean isExist = localIPsInSubnet.get(j).isExist();
                if (isExist) {
                    continue;
                }
                for (int k = 0; k < localIPsInSubnet.size(); k ++) {
                    if (k != j && isUsed[k] == false) {
                        String neighborIP = localIPsInSubnet.get(k).getIp();
                        NeighborEntry neighborEntry = new NeighborEntry();
                        neighborEntry.setLocalIp(localIP);
                        neighborEntry.setNeighborIp(neighborIP);
                        neighborEntry.setNeighborType(NeighborEntry.NeighborType.L2);
                        neighborTable.add(neighborEntry);
                    }
                }
                isUsed[j] = true;
            }
        }

        networkConfiguration.setNeighborTable(neighborTable);

        // set portEntities
        List<InternalPortEntity> portEntities = new ArrayList<>();
        for (Map.Entry<String, List<UTPortWithSubnetAndIPMapping>> entry : hostAndPortsMapping.entrySet()) {
            String bindingHostIP = entry.getKey();
            List<UTPortWithSubnetAndIPMapping> mapList = (List<UTPortWithSubnetAndIPMapping>)entry.getValue();
            for (UTPortWithSubnetAndIPMapping mapping : mapList) {
                List<PortEntity.FixedIp> fixedIps = mapping.getFixedIps();

                PortEntity portEntity = new PortEntity(DPMAutoUnitTestConstant.projectId, mapping.getPortId(),
                        mapping.getPortName(), "", DPMAutoUnitTestConstant.vpcId, true, mapping.getPortMacAddress(), mapping.getVethName(), fastPath,
                        null, null, null, fixedIps, null, null, null,
                        mapping.getBindingHostId(), null, null, null, null,
                        DPMAutoUnitTestConstant.networkNamespace, null, null, null, null, null,
                        null, false, null, null, 0, null, null,
                        false, false);

                InternalPortEntity port = new InternalPortEntity(portEntity, null, bindingHostIP);
                portEntities.add(port);
            }
        }

        networkConfiguration.setPortEntities(portEntities);

        // set vpcs
        List<VpcEntity> vpcs = new ArrayList<>();
        VpcEntity vpc = new VpcEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.vpcId, DPMAutoUnitTestConstant.vpcName,
                "", null, false, null, null, false, null,
                null, null, false, null, false, false, false,
                null, null, null, null, null, null, null,
                null, null, null, null, null, DPMAutoUnitTestConstant.cidr);
        vpcs.add(vpc);
        networkConfiguration.setVpcs(vpcs);

        // set subnets
        List<InternalSubnetEntity> subnets = new ArrayList<>();
        for (int i = 0; i < UTSubnets.size(); i ++) {
            UTSubnetInfo subnetInfo = UTSubnets.get(i);
            SubnetEntity subnetEntity = new SubnetEntity(DPMAutoUnitTestConstant.projectId, subnetInfo.getSubnetId(), subnetInfo.getSubnetName(), "", DPMAutoUnitTestConstant.vpcId,
                    subnetInfo.getSubnetCidr(), DPMAutoUnitTestConstant.availabilityZone, subnetInfo.getSubnetGatewayIP(), false, null,
                    null, null, DPMAutoUnitTestConstant.gatewayMacAddress, null,
                    null, null, null, null, null,
                    null, null, false, null, null,
                    null, false, null, null,
                    null, null, null, null, null,
                    null, null, false, null, null,
                    null);
            Long tunnelId = subnetInfo.getTunnelId();
            InternalSubnetEntity subnet = new InternalSubnetEntity(subnetEntity, tunnelId);
            subnets.add(subnet);
        }
        networkConfiguration.setSubnets(subnets);

        // set securityGroups
        List<SecurityGroup> securityGroups = new ArrayList<>();
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroups.add(securityGroup);
        networkConfiguration.setSecurityGroups(securityGroups);

        List<NeighborInfo> neighborINFO = new ArrayList<>();
        if (hasNeighbor) {
            for (Map.Entry<String, NeighborInfo> entry : neighborInfoMap.entrySet()) {
                NeighborInfo neighborInfo = (NeighborInfo)entry.getValue();
                neighborINFO.add(neighborInfo);
            }
        }
        networkConfiguration.setNeighborInfos(neighborINFO);

        return networkConfiguration;
    }

    public Map<String, Goalstate.GoalState> autoGenerateUTsOutput_MoreCustomizableScenarios(int operationType,
                                                                  int resourceType,
                                                                  Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap,
                                                                  Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap,
                                                                  List<UTSubnetInfo> UTSubnets,
                                                                  List<UTL3NeighborInfoMapping> L3NeighborInfoMapping,
                                                                  boolean hasInternalRouterInfo,
                                                                  boolean hasInternalSubnetRoutingTable,
                                                                  boolean hasInternalRoutingRule,
                                                                  boolean hasNeighbor,
                                                                  Map<String, UTNeighborInfoDetail> neighborInfoDetails,
                                                                  boolean fastPath) throws Exception {
        Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
        for (Map.Entry<String, List<UTPortWithSubnetAndIPMapping>> entry : existPortsMap.entrySet()) {
            HostGoalState hostGoalState = new HostGoalState();
            Map<String, List<UTPortWithSubnetAndIPMapping>> portsMap = new HashMap<>();
            portsMap.put(entry.getKey(), entry.getValue());
            NetworkConfiguration networkConfiguration = autoGenerateUTsInput_MoreCustomizableScenarios(operationType, resourceType, portsMap, UTSubnets, L3NeighborInfoMapping, hasInternalRouterInfo, hasInternalSubnetRoutingTable, hasInternalRoutingRule, hasNeighbor, neighborInfoDetails, fastPath);
            hostGoalState = buildHostGoalState(networkConfiguration, entry.getKey(), networkConfiguration.getPortEntities());
            goalStateHashMap.put(hostGoalState.getHostIp(), hostGoalState.getGoalState());
        }

        return goalStateHashMap;
    }

}
