package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.common.enumClass.OperationType;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalRoutingRule;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.logging.*;
import java.util.stream.Collectors;


public class GoalStateTransformer {
    private final GoalStateManager goalStateManager;

    public GoalStateTransformer(GoalStateManager goalStateManager) {
        this.goalStateManager = goalStateManager;
    }

    /**
     * transform client of dpm msg to aca protobuf format
     *
     * @param networkConfiguration msg to be transformmed
     * @return Map<String, Goalstate.GoalState>
     * @throws RuntimeException Various exceptions that may occur during the
     * send process
     */
    @Async
    public Future<Map<String, Goalstate.GoalState>> transformNorthToSouth(NetworkConfiguration networkConfiguration) throws RuntimeException {

        Map<String, String> ipPortIdMap = new ConcurrentHashMap<String, String>();
        Map<String, String> ipMacMap = new ConcurrentHashMap<String, String>();
        Map<String, String> ipSubnetIdMap = new ConcurrentHashMap<String, String>();
        Map<String, String> ipHostIpMap = new ConcurrentHashMap<String, String>();
        Map<String, Set<String>> hostIpFixedIpsMap = new ConcurrentHashMap<String, Set<String>>();
        Map<String, Set<String>> hostIpSubnetIdsMap = new ConcurrentHashMap<String, Set<String>>();
        Map<String, InternalSubnetEntity> subnetIdSubnetsMap = new ConcurrentHashMap<String, InternalSubnetEntity>();
        Map<String, InternalPortEntity> portIdPortMap = new ConcurrentHashMap<String, InternalPortEntity>();
        Map<String, NeighborInfo> portIdNeighborInfoMap = new ConcurrentHashMap<String, NeighborInfo>();

        // print entry input for debug usage
        goalStateManager.printNetworkConfiguration(networkConfiguration);
        goalStateManager.getDPMPreparer().convert(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap);

        Map<String, Set<String>> portsInSameSubnetMap = new ConcurrentHashMap<String, Set<String>>();

        Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap = new ConcurrentHashMap<String, Set<NeighborInfo>>();
        // L3
        Map<String, Set<String>> portsInSameVpcMap = new ConcurrentHashMap<String, Set<String>>();

        Map<String, Set<NeighborInfo>> neighborInfoInSameVpcMap = new ConcurrentHashMap<String, Set<NeighborInfo>>();

        InternalPortEntity[] portStatesArr = networkConfiguration.getPortEntities().toArray(new InternalPortEntity[0]);
        InternalSubnetEntity[] subnetArr = networkConfiguration.getSubnets().toArray(new InternalSubnetEntity[0]);
        VpcEntity[] vpcArr = networkConfiguration.getVpcs().toArray(new VpcEntity[0]);

        // TODO need to v2 subnet and vpc part when logic is
        //  clear and integration done
        Map<String, List<InternalPortEntity>> mapGroupedByHostIp = new ConcurrentHashMap();
        Map<String, InternalSubnetEntity> subnetMap = new ConcurrentHashMap<String, InternalSubnetEntity>();
        Map<String, InternalPortEntity> portMap = new ConcurrentHashMap<String, InternalPortEntity>();
        Map<String, VpcEntity> vpcMap = new ConcurrentHashMap<String, VpcEntity>();
        // construct map from list
        for (InternalSubnetEntity s : subnetArr) {
            subnetMap.put(s.getId(), s);
        }

        for (VpcEntity vpc : vpcArr) {
            vpcMap.put(vpc.getId(), vpc);
        }
        // group nb msg by ip
        int portCounter = 0;
        for (InternalPortEntity portEntityNB : portStatesArr) {
            portMap.put(portEntityNB.getId(), portEntityNB);
            String bindingHostIP = portEntityNB.getBindingHostIP();
            InternalPortEntity currentPortEntity = portStatesArr[portCounter];
            if (!mapGroupedByHostIp.containsKey(bindingHostIP)) {
                List<InternalPortEntity> portStates = new ArrayList<InternalPortEntity>();
                portCounter = goalStateManager.getGoalStateHelper().bindHostWithPorts(portsInSameSubnetMap, neighborInfoInSameSubenetMap, mapGroupedByHostIp, subnetMap, vpcMap, portCounter, bindingHostIP, currentPortEntity, portStates, 2);
            } else {
                List<InternalPortEntity> portStates = mapGroupedByHostIp.get(bindingHostIP);
                portCounter = goalStateManager.getGoalStateHelper().bindHostWithPorts(portsInSameSubnetMap, neighborInfoInSameSubenetMap, mapGroupedByHostIp, subnetMap, vpcMap, portCounter, bindingHostIP, currentPortEntity, portStates, 2);
            }
        }

        int portCounterL3 = 0;
        for (InternalPortEntity portEntityNB : portStatesArr) {
            portMap.put(portEntityNB.getId(), portEntityNB);
            String bindingHostIP = portEntityNB.getBindingHostIP();
            InternalPortEntity currentPortEntity = portStatesArr[portCounterL3];
            if (!mapGroupedByHostIp.containsKey(bindingHostIP)) {
                List<InternalPortEntity> portStates = new ArrayList<InternalPortEntity>();
                portCounterL3 = goalStateManager.getGoalStateHelper().bindHostWithPorts(portsInSameVpcMap, neighborInfoInSameVpcMap, mapGroupedByHostIp, subnetMap, vpcMap, portCounterL3, bindingHostIP, currentPortEntity, portStates, 3);
            } else {
                List<InternalPortEntity> portStates = mapGroupedByHostIp.get(bindingHostIP);
                portCounterL3 = goalStateManager.getGoalStateHelper().bindHostWithPorts(portsInSameVpcMap, neighborInfoInSameVpcMap, mapGroupedByHostIp, subnetMap, vpcMap, portCounterL3, bindingHostIP, currentPortEntity, portStates, 3);
            }
        }

        // construct sb msg by ip
        Map<String, Goalstate.GoalState> goalStateConcurrentHashMap = new ConcurrentHashMap<String, Goalstate.GoalState>();
        // TODO would opt this part when perf needed
        hostIpSubnetIdsMap.keySet().forEach(currentGroupHostIp -> {
            Set<Port.PortState> portStateHashSet = new HashSet<Port.PortState>();
            Map<String, Neighbor.NeighborState> neighborStates = new ConcurrentHashMap<String, Neighbor.NeighborState>();
            Set<Subnet.SubnetState> subnetStateSet = new HashSet();
            Set<Vpc.VpcState> vpcStateSet = new HashSet();
            Set<DHCP.DHCPState> dhcpStateList = new HashSet();
            final List<InternalPortEntity> internalPortEntitySet = mapGroupedByHostIp.get(currentGroupHostIp);
            if (internalPortEntitySet != null) {
                internalPortEntitySet.forEach(portStateWithEverythingFilledNB -> {
                    List<Port.PortConfiguration.FixedIp> fixedIps = new ArrayList();
                    boolean isExistingPort = false;
                    final List<PortEntity.FixedIp> fixedIps1 = portStateWithEverythingFilledNB.getFixedIps();
                    for (PortEntity.FixedIp fixedIp : fixedIps1) {
                        fixedIps.add(Port.PortConfiguration.FixedIp.newBuilder().setSubnetId(fixedIp.getSubnetId()).setIpAddress(fixedIp.getIpAddress()).build());
                        if (portIdNeighborInfoMap.values().stream().filter(e -> e.getPortId().equals(ipPortIdMap.get(fixedIp.getIpAddress()))).count() == 0) {
                            DHCP.DHCPConfiguration dhcpConfiguration = DHCP.DHCPConfiguration.newBuilder().setRevisionNumber(GoalStateManager.FORMAT_REVISION_NUMBER).setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER).setSubnetId(fixedIp.getSubnetId()).setMacAddress(portStateWithEverythingFilledNB.getMacAddress()).setIpv4Address(fixedIp.getIpAddress()).build();
                            DHCP.DHCPState dhcpState = DHCP.DHCPState.newBuilder().setConfiguration(dhcpConfiguration).build();
                            dhcpStateList.add(dhcpState);
                        }
                    }
                    String name = portStateWithEverythingFilledNB.getName() == null ? "" : portStateWithEverythingFilledNB.getName();

                    Port.PortConfiguration portConfiguration = Port.PortConfiguration.newBuilder().setName(name).setProjectId(portStateWithEverythingFilledNB.getProjectId()).setVpcId(portStateWithEverythingFilledNB.getVpcEntities().iterator().next().getId()).setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER).setAdminStateUp(true).setMacAddress(portStateWithEverythingFilledNB.getMacAddress()).setRevisionNumber(GoalStateManager.FORMAT_REVISION_NUMBER).addAllFixedIps(fixedIps).setId(portStateWithEverythingFilledNB.getId()).setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE).setMessageTypeValue(Common.MessageType.FULL_VALUE).build();

                    final Port.PortState portStateSB = Port.PortState.newBuilder().setConfiguration(portConfiguration).setOperationType(Common.OperationType.CREATE).build();

                    portStateHashSet.add(portStateSB);
                });
            }

            // avoid duplicate

            if (networkConfiguration.getNeighborTable() != null && neighborStates.size() == 0 && networkConfiguration.getNeighborTable().size() > 0) {

                Set<String> brandNewIps = new ConcurrentSkipListSet();
                if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() > 1) {
                    for (String ip : hostIpFixedIpsMap.values().stream().flatMap(alist -> alist.stream()).collect(Collectors.toList())) {
                        {
                            Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                            Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                            if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                                if (!hostIpSubnetIdsMap.get(currentGroupHostIp).iterator().next().equals(ipSubnetIdMap.get(ip))) {
                                    l2 = Neighbor.NeighborType.L3;
                                    l3 = Neighbor.NeighborType.L2;
                                }
                            }

                            if (!portIdNeighborInfoMap.containsKey(ip)) {
                                goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, ip, l3);
                            } // if contains ip in neighbor info
                        }
                    }
                } else if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                    for (String ip : hostIpFixedIpsMap.values().stream().flatMap(alist -> alist.stream()).collect(Collectors.toList())) {
                        Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                        Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                        if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                            if (!hostIpSubnetIdsMap.get(currentGroupHostIp).iterator().next().equals(ipSubnetIdMap.get(ip))) {
                                l2 = Neighbor.NeighborType.L3;
                                l3 = Neighbor.NeighborType.L2;
                            }
                        }

                        final InternalPortEntity portStateWithEverythingFilledNB = portIdPortMap.get(ipPortIdMap.get(ip));

                        if (!portIdNeighborInfoMap.containsKey(ip)) {
                            Set<InternalSubnetEntity> ss = portStateWithEverythingFilledNB.getSubnetEntities();
                            final InternalSubnetEntity next = ss.iterator().next();
                            if (ss.size() == 1 && ipSubnetIdMap.get(ip).equals(next.getId())) {

                                goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, ip, l2);
                            } else {
                                goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, ip, l3);
                            } // end if internal size==1 sn>2
                            brandNewIps.add(ip);
                        }
                    }
                    // start new loop for adding new ip to existing port

                } // size ==1
                for (String nip : brandNewIps) {
                    if (!ipHostIpMap.get(nip).equals(currentGroupHostIp))
                        continue;
                    for (String eip : portIdNeighborInfoMap.keySet()) {

                        Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                        Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                        if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                            if (!hostIpSubnetIdsMap.get(currentGroupHostIp).iterator().next().equals(ipSubnetIdMap.get(nip))) {
                                l2 = Neighbor.NeighborType.L3;
                                l3 = Neighbor.NeighborType.L2;
                            }
                        }

                        if (ipHostIpMap.get(eip).equals(currentGroupHostIp) && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) { // ip 2.2
                            goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, eip, l3);
                        } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp) && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {
                            goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, eip, l3);
                        } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp) && (ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {

                            goalStateManager.getGoalStateHelper().createNeighborHelper(networkConfiguration, ipPortIdMap, ipMacMap, ipSubnetIdMap, ipHostIpMap, hostIpFixedIpsMap, hostIpSubnetIdsMap, subnetIdSubnetsMap, portIdPortMap, portIdNeighborInfoMap, currentGroupHostIp, neighborStates, brandNewIps, eip, l2);
                        } // inner if end
                    } // 2nd loop end
                } // current nip end
            } // if size==0

            // lookup subnet entity
            for (String sid : ipSubnetIdMap.values()) {
                InternalSubnetEntity subnetEntity1 = subnetIdSubnetsMap.get(sid);
                if (subnetEntity1 == null) {
                    GoalStateManager.LOG.log(Level.SEVERE, sid +
                            "subnet is MISSING");
                    continue;
                }
                goalStateManager.getGoalStateHelper().add2SubnetStates(networkConfiguration, subnetStateSet, subnetEntity1);
                // lookup vpc entity
                final VpcEntity vpcEntity = vpcMap.get(subnetEntity1.getVpcId());
                Vpc.VpcConfiguration vpcConfiguration = Vpc.VpcConfiguration.newBuilder().setId(vpcEntity.getId()).setCidr(vpcEntity.getCidr()).setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER).setRevisionNumber(GoalStateManager.FORMAT_REVISION_NUMBER).build();
                Vpc.VpcState vpcState = Vpc.VpcState.newBuilder().setConfiguration(vpcConfiguration).setOperationTypeValue(Common.OperationType.CREATE_VALUE).setOperationType(Common.OperationType.CREATE).build();
                vpcStateSet.add(vpcState);
            }

            List<Router.RouterState> routerStateList = new ArrayList<Router.RouterState>();
            if (networkConfiguration.getInternalRouterInfos() != null && neighborStates.keySet().stream().filter(e -> e.indexOf("#L3") != -1).count() > 0) {
                for (InternalRouterInfo internalRouterInfo : networkConfiguration.getInternalRouterInfos()) {
                    final List<InternalSubnetRoutingTable> subnetRoutingTables = internalRouterInfo.getRouterConfiguration().getSubnetRoutingTables();
                    final List<Router.RouterConfiguration.SubnetRoutingTable> subnetRoutingTables2 = new ArrayList<Router.RouterConfiguration.SubnetRoutingTable>();
                    for (InternalSubnetRoutingTable internalSubnetRoutingTable : subnetRoutingTables) {
                        if (neighborStates.keySet().stream().filter(e -> e.indexOf("#L3") != -1).count() > 0) {
                            final InternalSubnetEntity subnetEntity1 = subnetIdSubnetsMap.get(internalSubnetRoutingTable.getSubnetId());

                            goalStateManager.getGoalStateHelper().add2SubnetStates(networkConfiguration, subnetStateSet, subnetEntity1);
                        }

                        Router.RouterConfiguration.SubnetRoutingTable subnetRoutingTable = Router.RouterConfiguration.SubnetRoutingTable.newBuilder().setSubnetId(internalSubnetRoutingTable.getSubnetId()).buildPartial();
                        List<Router.RouterConfiguration.RoutingRule> routingRuleList = new ArrayList<Router.RouterConfiguration.RoutingRule>();
                        for (InternalRoutingRule internalRoutingRule : internalSubnetRoutingTable.getRoutingRules()) {
                            Router.DestinationType destinationType =
                                    Router.DestinationType.INTERNET;
                            Router.RouterConfiguration.RoutingRuleExtraInfo routingRuleExtraInfo = Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder().setDestinationType(destinationType).setNextHopMac(internalRoutingRule.getRoutingRuleExtraInfo().getNextHopMac()).build();
                            Common.OperationType op = goalStateManager.getGoalStateHelper().getOperationType(internalRoutingRule.getOperationType().equals(OperationType.CREATE), internalRoutingRule.getOperationType().equals(OperationType.INFO), Common.OperationType.INFO, internalRoutingRule.getOperationType().equals(OperationType.DELETE), Common.OperationType.DELETE, internalRoutingRule.getOperationType().equals(OperationType.UPDATE), Common.OperationType.UPDATE);

                            Router.RouterConfiguration.RoutingRule routingRule = Router.RouterConfiguration.RoutingRule.newBuilder().setDestination(internalRoutingRule.getDestination()).setId(internalRoutingRule.getId()).setName(internalRoutingRule.getName()).setNextHopIp(internalRoutingRule.getNextHopIp()).setPriority(Integer.parseInt(internalRoutingRule.getPriority())).setOperationType(op).setRoutingRuleExtraInfo(routingRuleExtraInfo).build();
                            routingRuleList.add(routingRule);
                        }
                        subnetRoutingTables2.add(subnetRoutingTable.toBuilder().addAllRoutingRules(routingRuleList).build());
                    }

                    Router.RouterConfiguration routerConfiguration = Router.RouterConfiguration.newBuilder().setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER).setHostDvrMacAddress(internalRouterInfo.getRouterConfiguration().getHostDvrMac()).setId(internalRouterInfo.getRouterConfiguration().getId()).setMessageType(Common.MessageType.FULL).setRevisionNumber(GoalStateManager.FORMAT_REVISION_NUMBER).addAllSubnetRoutingTables(subnetRoutingTables2).build();
                    Router.RouterState routerState = Router.RouterState.newBuilder().setConfiguration(routerConfiguration).build();
                    routerStateList.add(routerState);
                }
            }
            // leave a dummy security group value since for now there is no impl for sg
            SecurityGroup.SecurityGroupConfiguration securityGroupConfiguration = SecurityGroup.SecurityGroupConfiguration.newBuilder().build();
            final SecurityGroup.SecurityGroupState securityGroupState = SecurityGroup.SecurityGroupState.newBuilder().setConfiguration(securityGroupConfiguration).build();
            final Goalstate.GoalState goalState = Goalstate.GoalState.newBuilder().addAllPortStates(portStateHashSet).addAllNeighborStates(neighborStates.values()).addAllSubnetStates(subnetStateSet).addSecurityGroupStates(0, securityGroupState).addAllRouterStates(routerStateList).addAllDhcpStates(dhcpStateList).build();
            goalStateConcurrentHashMap.put(currentGroupHostIp, goalState);
        });
        GoalStateManager.LOG.log(Level.INFO, goalStateConcurrentHashMap.entrySet().toString());
        return new AsyncResult<Map<String, Goalstate.GoalState>>(goalStateConcurrentHashMap);
    }
}