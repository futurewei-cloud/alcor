/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.common.enumClass.OperationType;
import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.dataplane.constants.DPMAutoUnitTestConstant;
import com.futurewei.alcor.dataplane.entity.*;
import com.futurewei.alcor.dataplane.exception.SecurityGroupNotFound;
import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.dataplane.exception.VpcEntityNotFound;
import com.futurewei.alcor.schema.Router;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DataPlaneManagerUtil {

    public List<List<InternalPortEntity>> getPortEntitiesList (int portNumPerHost,
                                                               int hostNum,
                                                               boolean hasInternalRouterInfo,
                                                               boolean fastPath) {
        List<List<InternalPortEntity>> portEntitiesList = new ArrayList<>();
        List<InternalPortEntity> portEntities = new ArrayList<>();
        for (int j = 0; j < hostNum; j ++) {
            List<InternalPortEntity> portEntitiesPerHost = new ArrayList<>();
            for (int i = 0; i < portNumPerHost; i ++) {
                int portCount = 0;
                List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
                PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(DPMAutoUnitTestConstant.subnetId + i, DPMAutoUnitTestConstant.IpAddress + i);
                fixedIps.add(fixedIp);

                PortEntity portEntity = new PortEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.portId + portCount,
                        DPMAutoUnitTestConstant.portName + portCount, "", DPMAutoUnitTestConstant.vpcId, true, DPMAutoUnitTestConstant.portMacAddress + portCount, DPMAutoUnitTestConstant.vethName + i, fastPath,
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
                portEntitiesPerHost.add(port);
                portCount ++;
            }
            portEntitiesList.add(portEntitiesPerHost);
        }
        return portEntitiesList;
    }

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
            for (int i = 0; i < subnetNum; i ++) {
                int Offset = i + 2;
                if (hasInternalSubnetRoutingTable) {
                    InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
                    List<InternalRoutingRule> routing_rules = new ArrayList<>();

                    if (hasInternalRoutingRule){
                        InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                        InternalRoutingRuleExtraInfo internalRoutingRuleExtraInfo = new InternalRoutingRuleExtraInfo();
                        internalRoutingRuleExtraInfo.setNextHopMac(DPMAutoUnitTestConstant.nextHopMac + i);
                        internalRoutingRule.setRoutingRuleExtraInfo(internalRoutingRuleExtraInfo);
                        internalRoutingRule.setOperationType(OperationType.CREATE);
                        internalRoutingRule.setDestination("10.0." + Offset + ".0/24");
                        internalRoutingRule.setId(DPMAutoUnitTestConstant.routeId + i);
                        internalRoutingRule.setName(DPMAutoUnitTestConstant.routeRuleName + i);
                        internalRoutingRule.setNextHopIp("10.0." + Offset + ".1");
                        internalRoutingRule.setPriority(DPMAutoUnitTestConstant.priority);
                        routing_rules.add(internalRoutingRule);
                    }

                    internalSubnetRoutingTable.setRoutingRules(routing_rules);
                    internalSubnetRoutingTable.setSubnetId(DPMAutoUnitTestConstant.subnetId + i);
                    subnet_routing_tables.add(internalSubnetRoutingTable);
                }
            }

            internalRouterConfiguration.setSubnetRoutingTables(subnet_routing_tables);
            internalRouterConfiguration.setHostDvrMac(DPMAutoUnitTestConstant.hostDvrMac);
            internalRouterConfiguration.setId(DPMAutoUnitTestConstant.routerConfigurationId);


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
//                neighborEntry.setNeighborIp(DPMAutoUnitTestConstant.L2neighborIp + NeighborIpAddressOffset);
//                neighborEntry.setNeighborType(NeighborEntry.NeighborType.L2);
                neighborTable.add(neighborEntry);
            }
        }
        networkConfiguration.setNeighborTable(neighborTable);

        // set portEntities
        List<InternalPortEntity> portEntities = new ArrayList<>();
        for (int j = 0; j < hostNum; j ++) {
            for (int i = 0; i < portNumPerHost; i ++) {
                int portCount = 0;
                List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
                PortEntity.FixedIp fixedIp = new PortEntity.FixedIp(DPMAutoUnitTestConstant.subnetId + i, DPMAutoUnitTestConstant.IpAddress + i);
                fixedIps.add(fixedIp);

                PortEntity portEntity = new PortEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.portId + portCount,
                        DPMAutoUnitTestConstant.portName + portCount, "", DPMAutoUnitTestConstant.vpcId, true, DPMAutoUnitTestConstant.portMacAddress + portCount, DPMAutoUnitTestConstant.vethName + i, fastPath,
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
                portCount ++;
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
            SubnetEntity subnetEntity = new SubnetEntity(DPMAutoUnitTestConstant.projectId, DPMAutoUnitTestConstant.subnetId + i, null, "", DPMAutoUnitTestConstant.vpcId,
                    "192.168." + IpAddressOffSet + ".0/24", null, "192.168." + IpAddressOffSet + ".1", false, null,
                    null, new GatewayPortDetail(DPMAutoUnitTestConstant.gatewayMacAddress, null), null,
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
            List<List<InternalPortEntity>> portEntitiesList = getPortEntitiesList (portNumPerHost, hostNum, hasInternalRouterInfo, fastPath);
            List<InternalPortEntity> portEntities = portEntitiesList.get(i);
            HostGoalState hostGoalState = new HostGoalState();
            NetworkConfiguration networkConfiguration = autoGenerateUTsInput(operationType, resourceType, portNumPerHost, hostNum, subnetNum, NumOfIPsInSubnet1, NumOfIPsInSubnet2, hasInternalRouterInfo, hasInternalSubnetRoutingTable, hasInternalRoutingRule, hasNeighbor, neighborNum, fastPath);
            hostGoalState = buildHostGoalState(networkConfiguration, DPMAutoUnitTestConstant.hostIp + i, portEntities);
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
        List<InternalSubnetEntity> subnets = networkConfig.getSubnets();
        if (subnets == null || subnets.size() == 0) {
            return;
        }

        for (InternalSubnetEntity subnetEntity: subnets) {
            Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
            subnetConfigBuilder.setId(subnetEntity.getId());
            subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
            subnetConfigBuilder.setVpcId(subnetEntity.getVpcId());
            if (subnetEntity.getName() != null) {
                subnetConfigBuilder.setName(subnetEntity.getName());
            }
            subnetConfigBuilder.setCidr(subnetEntity.getCidr());
            subnetConfigBuilder.setTunnelId(subnetEntity.getTunnelId());

            Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
            gatewayBuilder.setIpAddress(subnetEntity.getGatewayIp());
            gatewayBuilder.setMacAddress(subnetEntity.getGatewayPortDetail().getGatewayMacAddress());
            subnetConfigBuilder.setGateway(gatewayBuilder.build());
            subnetConfigBuilder.setDhcpEnable(subnetEntity.getDhcpEnable());

            // TODO: need to set DNS based on latest contract
            if (subnetEntity.getAvailabilityZone() != null) {
                subnetConfigBuilder.setAvailabilityZone(subnetEntity.getAvailabilityZone());
            }
            Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(com.futurewei.alcor.schema.Common.OperationType.INFO);
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
            portConfigBuilder.setRevisionNumber(DPMAutoUnitTestConstant.revisionNumber);
            portConfigBuilder.setId(portEntity.getId());
            portConfigBuilder.setUpdateType(Common.UpdateType.FULL);
            portConfigBuilder.setVpcId(portEntity.getVpcId());
            portConfigBuilder.setName(portEntity.getName());
            portConfigBuilder.setMacAddress(portEntity.getMacAddress());
            portConfigBuilder.setAdminStateUp(portEntity.getAdminStateUp());

            Port.PortConfiguration.HostInfo.Builder hostInfoBuilder = Port.PortConfiguration.HostInfo.newBuilder();
            hostInfoBuilder.setIpAddress(portEntity.getBindingHostIP());
            //TODO: Do we need mac address?
            //hostInfoBuilder.setMacAddress()
            //portConfigBuilder.setHostInfo(hostInfoBuilder.build());
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
    private NeighborInfo getNeighborInfo(NetworkConfiguration networkConfig, String hostIp, List<String> hostsHaveCreatedPort, boolean[] visited) throws Exception {
        NeighborInfo result = null;
        List<NeighborEntry> neighborTable = networkConfig.getNeighborTable();
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        List<InternalPortEntity> portEntities = networkConfig.getPortEntities();
        for (int i = 0; i < neighborInfos.size(); i ++) {
            NeighborInfo neighborInfo = neighborInfos.get(i);
            if (neighborInfo.getHostIp().equals(hostIp) && !visited[i]) {
                result = neighborInfo;
                visited[i] = true;
                break;
            }
        }

        if (result == null) {
            //throw new NeighborInfoNotFound();
            return new NeighborInfo();
        }

        return result;
    }

    private List<NeighborInfo> getNeighborInfos(NetworkConfiguration networkConfig, String hostIp, List<String> hostsHaveCreatedPort) throws Exception {
        List<NeighborInfo> result = new ArrayList<>();
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        List<String> createdPortIPInHost = new ArrayList<>();
        List<String> portIps = new ArrayList<>();

        List<InternalPortEntity> portEntities = networkConfig.getPortEntities();
        for (InternalPortEntity port : portEntities) {
            String bindingHostIP = port.getBindingHostIP();
            List<PortEntity.FixedIp> fixedIps = port.getFixedIps();
            String ipAddress = fixedIps.get(0).getIpAddress();
            String portId = port.getId();
            if (bindingHostIP.equals(hostIp)) {
                createdPortIPInHost.add(ipAddress);
            }
            for (NeighborInfo neighborInfo : neighborInfos) {
                NeighborInfo newNeighborInfo = new NeighborInfo();
                BeanUtils.copyProperties(neighborInfo, newNeighborInfo);
                String neighborHostIp = newNeighborInfo.getHostIp();
                if (bindingHostIP.equals(neighborHostIp)) {
                    newNeighborInfo.setPortIp(ipAddress);
                    newNeighborInfo.setPortId(portId);
                    if (!portIps.contains(newNeighborInfo.getPortIp())) {
                        result.add(newNeighborInfo);
                        portIps.add(newNeighborInfo.getPortIp());
                    }
                    break;
                }
            }
        }

        if (hostsHaveCreatedPort.contains(hostIp)){
            for (NeighborInfo neighborInfo : neighborInfos) {
                String neighborHostIp = neighborInfo.getHostIp();
                String[] portIp = neighborInfo.getPortIp().split("\\.");
                String[] createdPortIP = createdPortIPInHost.get(0).split("\\.");
//                if (neighborHostIp.equals(hostIp) && Integer.parseInt(portIp[portIp.length - 2]) < Integer.parseInt(createdPortIP[createdPortIP.length - 2])) {
//                    continue;
//                }
                if (!portIps.contains(neighborInfo.getPortIp())) {
                    result.add(neighborInfo);
                    portIps.add(neighborInfo.getPortIp());
                }
            }
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
    private void buildNeighborState(NetworkConfiguration networkConfig, String hostIp, List<InternalPortEntity> portEntities, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }
        boolean[] visited = new boolean[neighborInfos.size()];

        List<NeighborEntry> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<String> hostsHaveCreatedPort = new ArrayList<>();
        for (InternalPortEntity port : networkConfig.getPortEntities()) {
            hostsHaveCreatedPort.add(port.getBindingHostIP());
        }

        List<NeighborInfo> neighborInfosInHost = getNeighborInfos(networkConfig, hostIp, hostsHaveCreatedPort);

        for (NeighborInfo neighborInfo: neighborInfosInHost) {

//            NeighborInfo neighborInfo = getNeighborInfo(networkConfig, hostIp, hostsHaveCreatedPort, visited);
//            if (!hostIp.equals(neighborInfo.getHostIp())) {
//                continue;
//            }
            Neighbor.NeighborConfiguration.Builder neighborConfigBuilder = Neighbor.NeighborConfiguration.newBuilder();
            neighborConfigBuilder.setId(neighborInfo.getPortId());
            String neighborTypeStr = null;
            for (NeighborEntry neighborEntry : neighborTable) {
                String neighborIp = neighborEntry.getNeighborIp();
                if (neighborIp.equals(neighborInfo.getPortIp())) {
                    neighborTypeStr = neighborEntry.getNeighborType().getType();
                    break;
                }
            }
            Neighbor.NeighborType neighborType = null;
            if (neighborTypeStr != null) {
                neighborType = Neighbor.NeighborType.valueOf(neighborTypeStr);
            }
            //neighborConfigBuilder.setNeighborType(neighborType);
            neighborConfigBuilder.setRevisionNumber(DPMAutoUnitTestConstant.revisionNumber2);
            neighborConfigBuilder.setVpcId(neighborInfo.getVpcId());
            //neighborConfigBuilder.setName();
            neighborConfigBuilder.setMacAddress(neighborInfo.getPortMac());
            neighborConfigBuilder.setHostIpAddress(neighborInfo.getHostIp());
            //TODO:setNeighborHostDvrMac
            //neighborConfigBuilder.setNeighborHostDvrMac();
            Neighbor.NeighborConfiguration.FixedIp.Builder fixedIpBuilder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
            fixedIpBuilder.setSubnetId(neighborInfo.getSubnetId());
            fixedIpBuilder.setIpAddress(neighborInfo.getPortIp());
            if (neighborType != null) {
                fixedIpBuilder.setNeighborType(neighborType);
            }
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
        List<SecurityGroup> securityGroups = networkConfig.getSecurityGroups();
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

        com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupState.newBuilder();
        com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigBuilder = com.futurewei.alcor.schema.SecurityGroup.SecurityGroupConfiguration.newBuilder();
        for (String securityGroupId: securityGroupIds) {
            SecurityGroup securityGroup = getSecurityGroup(networkConfig, securityGroupId);
            securityGroupConfigBuilder.setId(securityGroup.getId());
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

        }
        securityGroupStateBuilder.setOperationType(networkConfig.getOpType());
        securityGroupStateBuilder.setConfiguration(securityGroupConfigBuilder.build());
        goalStateBuilder.addSecurityGroupStates(securityGroupStateBuilder.build());
    }

    /**
     * build DhcpState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildDhcpState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder, List<InternalPortEntity> portEntities) throws Exception {
        if (portEntities == null || portEntities.size() == 0) {
            return;
        }

        List<Port.PortState> portStates = goalStateBuilder.getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        //for (int i = 0; i < portStates.size(); i ++) {
        for (int i = portStates.size() - 1; i >= 0; i --) {
            Port.PortState portState = portStates.get(i);
            String macAddress = portState.getConfiguration().getMacAddress();
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            if (fixedIps == null || fixedIps.size() == 0) {
                continue;
            }

            DHCP.DHCPConfiguration.Builder dhcpConfigBuilder = DHCP.DHCPConfiguration.newBuilder();
            dhcpConfigBuilder.setMacAddress(macAddress);
            dhcpConfigBuilder.setIpv4Address(fixedIps.get(0).getIpAddress());
            dhcpConfigBuilder.setRevisionNumber(DPMAutoUnitTestConstant.revisionNumber);
            dhcpConfigBuilder.setSubnetId(fixedIps.get(0).getSubnetId());
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

    /**
     * build RouterState in GoalState
     * @param networkConfig
     * @param goalStateBuilder
     * @throws Exception
     */
    private void buildRouterState(NetworkConfiguration networkConfig, Goalstate.GoalState.Builder goalStateBuilder) throws Exception {
        List<InternalSubnetEntity> subnets = networkConfig.getSubnets();
        if (subnets == null || subnets.size() < 2) {
            return;
        }

        List<InternalRouterInfo> internalRouterInfos = networkConfig.getInternalRouterInfos();
        if (internalRouterInfos == null || internalRouterInfos.size() == 0) {
            return;
        }

        Router.RouterConfiguration.Builder routerConfigBuilder = Router.RouterConfiguration.newBuilder();
        for (InternalRouterInfo routerInfo: internalRouterInfos) {
            InternalRouterConfiguration configuration = routerInfo.getRouterConfiguration();
            if (configuration == null) {
                continue;
            }
            routerConfigBuilder.setRevisionNumber(DPMAutoUnitTestConstant.revisionNumber);
            if (configuration.getRequestId() != null) {
                routerConfigBuilder.setRequestId(configuration.getRequestId());
            }
            routerConfigBuilder.setId(configuration.getId());
            routerConfigBuilder.setUpdateType(Common.UpdateType.FULL);
            routerConfigBuilder.setHostDvrMacAddress(configuration.getHostDvrMac());

            List<InternalSubnetRoutingTable> subnetRoutingTables = configuration.getSubnetRoutingTables();

            if (subnetRoutingTables != null && subnetRoutingTables.size() != 0) {
                for (int i = 0 ; i < subnetRoutingTables.size(); i ++) {
                    Router.RouterConfiguration.SubnetRoutingTable.Builder subnetRoutingTableBuilder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
                    InternalSubnetRoutingTable subnetRoutingTable = subnetRoutingTables.get(i);
                    List<InternalRoutingRule> routingRules = subnetRoutingTable.getRoutingRules();
                    if (routingRules != null && routingRules.size() != 0) {
                        for (int j = 0 ; j < routingRules.size(); j ++) {
                            InternalRoutingRule internalRoutingRule = routingRules.get(j);
                            Router.RouterConfiguration.RoutingRule.Builder routingRule = Router.RouterConfiguration.RoutingRule.newBuilder();
                            routingRule.setDestination(internalRoutingRule.getDestination());
                            routingRule.setId(internalRoutingRule.getId());
                            routingRule.setName(internalRoutingRule.getName());
                            routingRule.setPriority(internalRoutingRule.getPriority());
                            routingRule.setNextHopIp(internalRoutingRule.getNextHopIp());

                            InternalRoutingRuleExtraInfo internalRoutingRuleExtraInfo = internalRoutingRule.getRoutingRuleExtraInfo();
                            Router.RouterConfiguration.RoutingRuleExtraInfo.Builder routingRuleExtraInfo = Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder();
                            routingRuleExtraInfo.setNextHopMac(internalRoutingRuleExtraInfo.getNextHopMac());
                            routingRule.setRoutingRuleExtraInfo(routingRuleExtraInfo);

                            subnetRoutingTableBuilder.addRoutingRules(routingRule);
                        }
                    }

                    subnetRoutingTableBuilder.setSubnetId(subnetRoutingTable.getSubnetId());
                    routerConfigBuilder.addSubnetRoutingTables(subnetRoutingTableBuilder);
                }

            }

        }



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
        buildNeighborState(networkConfig, hostIp, portEntities, goalStateBuilder);
        buildSecurityGroupState(networkConfig, goalStateBuilder);
        buildDhcpState(networkConfig, goalStateBuilder, portEntities);
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
            for (int i = 0; i < UTSubnets.size(); i ++) {
                int Offset = i + 2;
                if (hasInternalSubnetRoutingTable) {
                    InternalSubnetRoutingTable internalSubnetRoutingTable = new InternalSubnetRoutingTable();
                    List<InternalRoutingRule> routing_rules = new ArrayList<>();

                    if (hasInternalRoutingRule){
                        InternalRoutingRule internalRoutingRule = new InternalRoutingRule();
                        InternalRoutingRuleExtraInfo internalRoutingRuleExtraInfo = new InternalRoutingRuleExtraInfo();
                        internalRoutingRuleExtraInfo.setNextHopMac(DPMAutoUnitTestConstant.nextHopMac + i);
                        internalRoutingRule.setRoutingRuleExtraInfo(internalRoutingRuleExtraInfo);
                        internalRoutingRule.setOperationType(OperationType.CREATE);
                        internalRoutingRule.setDestination("10.0." + Offset + ".0/24");
                        internalRoutingRule.setId(DPMAutoUnitTestConstant.routeId + i);
                        internalRoutingRule.setName(DPMAutoUnitTestConstant.routeRuleName + i);
                        internalRoutingRule.setNextHopIp("10.0." + Offset + ".1");
                        internalRoutingRule.setPriority(DPMAutoUnitTestConstant.priority);
                        routing_rules.add(internalRoutingRule);
                    }

                    internalSubnetRoutingTable.setRoutingRules(routing_rules);
                    internalSubnetRoutingTable.setSubnetId(DPMAutoUnitTestConstant.subnetId + i);
                    subnet_routing_tables.add(internalSubnetRoutingTable);
                }
            }

            internalRouterConfiguration.setSubnetRoutingTables(subnet_routing_tables);
            internalRouterConfiguration.setHostDvrMac(DPMAutoUnitTestConstant.hostDvrMac);
            internalRouterConfiguration.setId(DPMAutoUnitTestConstant.routerConfigurationId);


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
                for (int k = 0; k < L3NeighborInfoMapping.size(); k ++) {
                    if (k != i) {
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
        }

        // configure L2
        for (int i = 0; i < L3NeighborInfoMapping.size(); i ++) {
            UTL3NeighborInfoMapping local = L3NeighborInfoMapping.get(i);
            List<UTIPInfo> localIPsInSubnet = local.getIPsInSubnet();
            boolean[] isUsed = new boolean[localIPsInSubnet.size() + 1];
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
            SubnetEntity subnetEntity = new SubnetEntity(DPMAutoUnitTestConstant.projectId, subnetInfo.getSubnetId(), null, "", DPMAutoUnitTestConstant.vpcId,
                    subnetInfo.getSubnetCidr(), null, subnetInfo.getSubnetGatewayIP(), false, null,
                    null, new GatewayPortDetail(DPMAutoUnitTestConstant.gatewayMacAddress, null), null,
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
        NetworkConfiguration networkConfiguration = autoGenerateUTsInput_MoreCustomizableScenarios(operationType, resourceType, createPortsMap, UTSubnets, L3NeighborInfoMapping, hasInternalRouterInfo, hasInternalSubnetRoutingTable, hasInternalRoutingRule, hasNeighbor, neighborInfoDetails, fastPath);
        for (Map.Entry<String, List<UTPortWithSubnetAndIPMapping>> entry : createPortsMap.entrySet()) {
            HostGoalState hostGoalState = new HostGoalState();
            List<InternalPortEntity> portEntities = new ArrayList<>();
            String hostIp = entry.getKey();
            Map<String, List<UTPortWithSubnetAndIPMapping>> portsMap = new HashMap<>();
            portsMap.put(entry.getKey(), entry.getValue());

            List<InternalPortEntity> allPortEntities = networkConfiguration.getPortEntities();
            for (InternalPortEntity port : allPortEntities) {
                if (port.getBindingHostIP().equals(hostIp)) {
                    portEntities.add(port);
                }
            }

            hostGoalState = buildHostGoalState(networkConfiguration, entry.getKey(), portEntities);
            goalStateHashMap.put(hostGoalState.getHostIp(), hostGoalState.getGoalState());
        }

        return goalStateHashMap;
    }

}
