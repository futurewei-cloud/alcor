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

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalRoutingRule;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;
import static com.futurewei.alcor.schema.Port.PortConfiguration.REVISION_NUMBER_FIELD_NUMBER;

@Component
public class GoalStateManager {
  public static final int FORMAT_REVISION_NUMBER = 1;
  @Autowired private GoalStateService goalStateService;
  private static final Logger LOG = LoggerFactory.getLogger();
  Map<String, String> ipPortIdMap = new HashMap<>();
  Map<String, String> ipMacMap = new HashMap<>();
  Map<String, String> ipSubnetIdMap = new HashMap<>();
  Map<String, String> ipHostIpMap = new HashMap<>();
  Map<String, Set<String>> hostIpFixedIpsMap = new HashMap<>();
  Map<String, Set<String>> hostIpSubnetIdsMap = new HashMap<>();
  Map<String, SubnetEntity> subnetIdSubnetsMap = new HashMap<>();
  Map<String, InternalPortEntity> portIdPortMap = new HashMap<>();
  Map<String, NeighborInfo> portIdNeighborInfoMap = new HashMap<>();

  private void convert(NetworkConfiguration networkConfiguration) {

    //      NetworkConfiguration result=new NetworkConfiguration();
    //      result.setInternalRouterInfos(networkConfiguration.getInternalRouterInfos())
    final List<NeighborInfo> neighborInfos = networkConfiguration.getNeighborInfos();
    final List<NeighborEntry> neighborTable = networkConfiguration.getNeighborTable();

    final List<InternalPortEntity> portEntities = networkConfiguration.getPortEntities();
    for (SubnetEntity s : networkConfiguration.getSubnets()) {
      subnetIdSubnetsMap.put(s.getId(), s);
    }
    for (InternalPortEntity internalPortEntity : portEntities) {
      portIdPortMap.put(internalPortEntity.getId(), internalPortEntity);

      //          for(NeighborInfo neighborInfo:neighborInfos)
      //          {
      for (PortEntity.FixedIp ip : internalPortEntity.getFixedIps()) {
        Set<String> strings = hostIpFixedIpsMap.get(internalPortEntity.getBindingHostIP());
        if (strings == null) strings = new TreeSet<>();
        Set<String> strings2 = hostIpSubnetIdsMap.get(internalPortEntity.getBindingHostIP());
        if (strings2 == null) strings2 = new TreeSet<>();
        strings.add(ip.getIpAddress());
        strings2.add(ip.getSubnetId());
        hostIpFixedIpsMap.put(internalPortEntity.getBindingHostIP(), strings);
        hostIpSubnetIdsMap.put(internalPortEntity.getBindingHostIP(), strings2);

        ipPortIdMap.put(ip.getIpAddress(), internalPortEntity.getId());
        ipHostIpMap.put(ip.getIpAddress(), internalPortEntity.getBindingHostIP());
        ipSubnetIdMap.put(ip.getIpAddress(), ip.getSubnetId());
        ipMacMap.put(ip.getIpAddress(), internalPortEntity.getMacAddress());
      }

      //              if(internalPortEntity.getFixedIps().contains(neighborInfo.getHostIp()))
      //          }
    }

    for (NeighborInfo internalPortEntity : neighborInfos) {
      //          portIdPortMap.put(internalPortEntity.getId(),internalPortEntity);

      //          for(NeighborInfo neighborInfo:neighborInfos)
      //          {
      //          for(PortEntity.FixedIp ip:internalPortEntity.getFixedIps())
      //          {
      ipPortIdMap.put(internalPortEntity.getPortIp(), internalPortEntity.getPortId());
      ipHostIpMap.put(internalPortEntity.getPortIp(), internalPortEntity.getHostIp());

      //            final InternalPortEntity internalPortEntity1 =
      //                    new InternalPortEntity();
      //            internalPortEntity1.setId(internalPortEntity.getPortId());
      //            internalPortEntity1.setBindingHostIP(internalPortEntity.getHostIp
      portIdNeighborInfoMap.put(internalPortEntity.getPortIp(), internalPortEntity);

      //          for(NeighborInfo neighborInfo:neighborInfos)
      //          {
      //            for(PortEntity.FixedIp ip:internalPortEntity.getFixedIps())
      //            {
      String ip = internalPortEntity.getPortIp();
      Set<String> strings = hostIpFixedIpsMap.get(internalPortEntity.getHostIp());
      if (strings == null) strings = new TreeSet<>();
      Set<String> strings2 = hostIpSubnetIdsMap.get(internalPortEntity.getHostIp());
      if (strings2 == null) strings2 = new TreeSet<>();
      strings.add(ip);
      strings2.add(internalPortEntity.getSubnetId());
      hostIpFixedIpsMap.put(internalPortEntity.getHostIp(), strings);
      hostIpSubnetIdsMap.put(internalPortEntity.getHostIp(), strings2);
      ipPortIdMap.put(ip, internalPortEntity.getPortId());
      ipHostIpMap.put(ip, internalPortEntity.getHostIp());
      ipSubnetIdMap.put(ip, internalPortEntity.getSubnetId());
      ipMacMap.put(ip, internalPortEntity.getPortMac());
    }
  }

  private void printNetworkConfiguration(NetworkConfiguration networkConfiguration) {
    LOG.log(Level.INFO, "### networkConf str: " + networkConfiguration.toString());
    ExclusionStrategy myExclusionStrategy =
        new ExclusionStrategy() {
          @Override
          public boolean shouldSkipField(FieldAttributes fa) {
            return fa.getName().equals("tenantId");
          }

          @Override
          public boolean shouldSkipClass(Class<?> clazz) {
            return false;
          }
        };
    Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
    LOG.log(Level.INFO, "###############");
    LOG.log(Level.INFO, gson.toJson(networkConfiguration));
  }
  /**
   * transform client of dpm msg to aca protobuf format
   *
   * @param networkConfiguration msg to be transformmed
   * @return Map<String, Goalstate.GoalState>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  public Map<String, Goalstate.GoalState> transformNorthToSouth(
      NetworkConfiguration networkConfiguration) throws RuntimeException {
    // print entry input
    printNetworkConfiguration(networkConfiguration);
    convert(networkConfiguration);
    System.out.println("after #### ");
    printNetworkConfiguration(networkConfiguration);

    Map<String, Set<String>> portsInSameSubnetMap = new HashMap<>();

    Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap = new HashMap<>();
    // L3
    Map<String, Set<String>> portsInSameVpcMap = new HashMap<>();

    Map<String, Set<NeighborInfo>> neighborInfoInSameVpcMap = new HashMap<>();

    InternalPortEntity[] portStatesArr =
        networkConfiguration.getPortEntities().toArray(new InternalPortEntity[0]);
    InternalSubnetEntity[] subnetArr =
        networkConfiguration.getSubnets().toArray(new InternalSubnetEntity[0]);
    com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcArr =
        networkConfiguration.getVpcs().toArray(new com.futurewei.alcor.web.entity.vpc.VpcEntity[0]);

    // TODO need to refactor subnet and vpc part when logic is
    //  clear and integration done
    Map<String, List<InternalPortEntity>> mapGroupedByHostIp = new HashMap();
    Map<String, InternalSubnetEntity> subnetMap = new HashMap<>();
    Map<String, InternalPortEntity> portMap = new HashMap<>();
    Map<String, com.futurewei.alcor.web.entity.vpc.VpcEntity> vpcMap = new HashMap<>();
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
        List<InternalPortEntity> portStates = new ArrayList<>();
        portCounter =
            bindHostWithPorts(
                portsInSameSubnetMap,
                neighborInfoInSameSubenetMap,
                mapGroupedByHostIp,
                subnetMap,
                vpcMap,
                portCounter,
                bindingHostIP,
                currentPortEntity,
                portStates,
                2);
      } else {
        List<InternalPortEntity> portStates = mapGroupedByHostIp.get(bindingHostIP);
        portCounter =
            bindHostWithPorts(
                portsInSameSubnetMap,
                neighborInfoInSameSubenetMap,
                mapGroupedByHostIp,
                subnetMap,
                vpcMap,
                portCounter,
                bindingHostIP,
                currentPortEntity,
                portStates,
                2);
      }
    }

    int portCounterL3 = 0;
    for (InternalPortEntity portEntityNB : portStatesArr) {
      portMap.put(portEntityNB.getId(), portEntityNB);
      String bindingHostIP = portEntityNB.getBindingHostIP();
      InternalPortEntity currentPortEntity = portStatesArr[portCounterL3];
      if (!mapGroupedByHostIp.containsKey(bindingHostIP)) {
        List<InternalPortEntity> portStates = new ArrayList<>();
        portCounterL3 =
            bindHostWithPorts(
                portsInSameVpcMap,
                neighborInfoInSameVpcMap,
                mapGroupedByHostIp,
                subnetMap,
                vpcMap,
                portCounterL3,
                bindingHostIP,
                currentPortEntity,
                portStates,
                3);
      } else {
        List<InternalPortEntity> portStates = mapGroupedByHostIp.get(bindingHostIP);
        portCounterL3 =
            bindHostWithPorts(
                portsInSameVpcMap,
                neighborInfoInSameVpcMap,
                mapGroupedByHostIp,
                subnetMap,
                vpcMap,
                portCounterL3,
                bindingHostIP,
                currentPortEntity,
                portStates,
                3);
      }
    }

    // construct sb msg by ip
    Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
    // TODO would opt this part when perf needed
    hostIpSubnetIdsMap
        .keySet()
        .forEach(
            currentGroupHostIp -> {
              Set<PortState> portStateHashSet = new HashSet<>();
              Set<Neighbor.NeighborState> neighborStates = new HashSet<>();
              Set<Subnet.SubnetState> subnetStateSet = new HashSet();
              Set<Vpc.VpcState> vpcStateSet = new HashSet();
              Set<DHCP.DHCPState> dhcpStateList = new HashSet();

              final List<InternalPortEntity> internalPortEntitySet =
                  mapGroupedByHostIp.get(currentGroupHostIp);
              if (internalPortEntitySet != null) {
                internalPortEntitySet.stream()
                    .forEach(
                        portStateWithEverythingFilledNB -> {
                          List<FixedIp> fixedIps = new ArrayList();

                          for (PortEntity.FixedIp fixedIp :
                              portStateWithEverythingFilledNB.getFixedIps()) {
                            boolean isExistingPort = false;
                            for (NeighborInfo internalPortEntity : portIdNeighborInfoMap.values()) {
                              if (internalPortEntity
                                  .getPortId()
                                  .equals(ipPortIdMap.get(fixedIp.getIpAddress()))) {
                                isExistingPort = true;
                                break;
                              }
                            }
                            if (!isExistingPort) {
                              DHCP.DHCPConfiguration dhcpConfiguration =
                                  DHCP.DHCPConfiguration.newBuilder()
                                      .setRevisionNumber(FORMAT_REVISION_NUMBER)
                                      .setFormatVersion(FORMAT_REVISION_NUMBER)
                                      .setSubnetId(fixedIp.getSubnetId())
                                      .setMacAddress(
                                          portStateWithEverythingFilledNB.getMacAddress())
                                      .setIpv4Address(fixedIp.getIpAddress())
                                      .build();
                              DHCP.DHCPState dhcpState =
                                  DHCP.DHCPState.newBuilder()
                                      .setConfiguration(dhcpConfiguration)
                                      .build();
                              dhcpStateList.add(dhcpState);
                            }
                          }
                          String name =
                              portStateWithEverythingFilledNB.getName() == null
                                  ? ""
                                  : portStateWithEverythingFilledNB.getName();

                          Port.PortConfiguration portConfiguration =
                              Port.PortConfiguration.newBuilder()
                                  .setName(name)
                                  .setProjectId(portStateWithEverythingFilledNB.getProjectId())
                                  .setVpcId(
                                      portStateWithEverythingFilledNB
                                          .getSubnetEntities()
                                          .iterator()
                                          .next()
                                          .getVpcId())
                                  .setFormatVersion(FORMAT_REVISION_NUMBER)
                                  .setAdminStateUp(true)
                                  .setMacAddress(portStateWithEverythingFilledNB.getMacAddress())
                                  .setRevisionNumber(FORMAT_REVISION_NUMBER)
                                  .addAllFixedIps(fixedIps)
                                  .buildPartial();

                          Port.PortConfiguration portConfiguration2 =
                              portConfiguration
                                  .toBuilder()
                                  .setId(portStateWithEverythingFilledNB.getId())
                                  .setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE)
                                  .setMessageTypeValue(Common.MessageType.FULL_VALUE)
                                  .build();

                          final PortState portStateSB =
                              PortState.newBuilder()
                                  .setConfiguration(portConfiguration2)
                                  .setOperationType(Common.OperationType.CREATE)
                                  .build();

                          portStateHashSet.add(portStateSB);
                        });
              }

              // avoid duplicate
              if (neighborStates.size() == 0) {
                Set<String> brandNewIps = new TreeSet();
                if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() > 1) {
                  for (String ip :
                      hostIpFixedIpsMap.values().stream()
                          .flatMap(alist -> alist.stream())
                          .collect(Collectors.toList())) {
                    {
                      final InternalPortEntity portStateWithEverythingFilledNB =
                          portIdPortMap.get(ipPortIdMap.get(ip));
                      if (!portIdNeighborInfoMap.containsKey(ip)) {
                        createNeighborState(
                            networkConfiguration,
                            neighborStates,
                            brandNewIps,
                            ip,
                            portStateWithEverythingFilledNB,
                            Neighbor.NeighborType.L3);
                      } // if contains ip in neighbor info
                    }
                  }
                } else if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                  for (String ip :
                      hostIpFixedIpsMap.values().stream()
                          .flatMap(alist -> alist.stream())
                          .collect(Collectors.toList())) {
                    final InternalPortEntity portStateWithEverythingFilledNB =
                        portIdPortMap.get(ipPortIdMap.get(ip));

                    if (!portIdNeighborInfoMap.containsKey(ip)) {
                      Set<InternalSubnetEntity> ss =
                          portStateWithEverythingFilledNB.getSubnetEntities();
                      final InternalSubnetEntity next = ss.iterator().next();
                      if (ss.size() == 1 && ipSubnetIdMap.get(ip).equals(next.getId())) {

                        createNeighborState(
                            networkConfiguration,
                            neighborStates,
                            brandNewIps,
                            ip,
                            portStateWithEverythingFilledNB,
                            Neighbor.NeighborType.L2);
                      } else {
                        createNeighborState(
                            networkConfiguration,
                            neighborStates,
                            brandNewIps,
                            ip,
                            portStateWithEverythingFilledNB,
                            Neighbor.NeighborType.L3);
                      } // end if internal size==1 sn>2
                      brandNewIps.add(ip);
                    }
                  }
                  // start new loop for adding new ip to existing port

                } // size ==1
                for (String nip : brandNewIps) {
                  if (!ipHostIpMap.get(nip).equals(currentGroupHostIp)) continue;
                  for (String eip : portIdNeighborInfoMap.keySet()) {
                    final InternalPortEntity portStateWithEverythingFilledNB =
                        portIdPortMap.get(ipPortIdMap.get(eip));
                    if (ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) { // ip 2.2
                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          portStateWithEverythingFilledNB,
                          Neighbor.NeighborType.L3);
                    } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {
                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          portStateWithEverythingFilledNB,
                          Neighbor.NeighborType.L3);
                    } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {

                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          portStateWithEverythingFilledNB,
                          Neighbor.NeighborType.L2);
                    } // inner if end
                  } // 2nd loop end
                } // current nip end
              } // if size==0

              // lookup subnet entity
              for (String sid : hostIpSubnetIdsMap.get(currentGroupHostIp)) {
                SubnetEntity subnetEntity1 = subnetIdSubnetsMap.get(sid);
                Subnet.SubnetConfiguration subnetConfiguration =
                    Subnet.SubnetConfiguration.newBuilder()
                        .setId(subnetEntity1.getId())
                        .setVpcId(subnetEntity1.getVpcId())
                        .setProjectId(subnetEntity1.getProjectId())
                        .setCidr(subnetEntity1.getCidr())
                        .setFormatVersion(FORMAT_REVISION_NUMBER)
                        .build();
                Subnet.SubnetState subnetState =
                    Subnet.SubnetState.newBuilder()
                        .setConfiguration(subnetConfiguration)
                        .buildPartial();
                if (networkConfiguration.getRsType().equals(Common.ResourceType.PORT))
                  subnetState =
                      subnetState.toBuilder().setOperationType(Common.OperationType.INFO).build();
                else
                  subnetState =
                      subnetState.toBuilder().setOperationType(Common.OperationType.CREATE).build();

                subnetStateSet.add(subnetState);
                // lookup vpc entity
                final VpcEntity vpcEntity = vpcMap.get(subnetEntity1.getVpcId());
                Vpc.VpcConfiguration vpcConfiguration =
                    Vpc.VpcConfiguration.newBuilder()
                        .setId(vpcEntity.getId())
                        .setCidr(vpcEntity.getCidr())
                        .setFormatVersion(FORMAT_REVISION_NUMBER)
                        .setRevisionNumber(FORMAT_REVISION_NUMBER)
                        .build();
                Vpc.VpcState vpcState =
                    Vpc.VpcState.newBuilder()
                        .setConfiguration(vpcConfiguration)
                        .setOperationTypeValue(Common.OperationType.CREATE_VALUE)
                        .setOperationType(Common.OperationType.CREATE)
                        .build();
                vpcStateSet.add(vpcState);
              }

              List<Router.RouterState> routerStateList = new ArrayList<>();

              for (InternalRouterInfo internalRouterInfo :
                  networkConfiguration.getInternalRouterInfos()) {
                final List<InternalSubnetRoutingTable> subnetRoutingTables =
                    internalRouterInfo.getRouterConfiguration().getSubnetRoutingTables();
                final List<Router.RouterConfiguration.SubnetRoutingTable> subnetRoutingTables2 =
                    new ArrayList<>();
                for (InternalSubnetRoutingTable internalSubnetRoutingTable : subnetRoutingTables) {
                  Router.RouterConfiguration.SubnetRoutingTable subnetRoutingTable =
                      Router.RouterConfiguration.SubnetRoutingTable.newBuilder()
                          .setSubnetId(internalSubnetRoutingTable.getSubnetId())
                          .buildPartial();
                  List<Router.RouterConfiguration.RoutingRule> routingRuleList = new ArrayList<>();
                  for (InternalRoutingRule internalRoutingRule :
                      internalSubnetRoutingTable.getRoutingRules()) {
                    Router.DestinationType destinationType = Router.DestinationType.INTERNET;
                    Router.RouterConfiguration.RoutingRuleExtraInfo routingRuleExtraInfo =
                        Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder()
                            .setDestinationType(destinationType)
                            .setNextHopMac(
                                internalRoutingRule.getRoutingRuleExtraInfo().getNextHopMac())
                            .build();
                    Router.RouterConfiguration.RoutingRule routingRule =
                        Router.RouterConfiguration.RoutingRule.newBuilder()
                            .setDestination(internalRoutingRule.getDestination())
                            .setId(internalRoutingRule.getId())
                            .setName(internalRoutingRule.getName())
                            .setNextHopIp(internalRoutingRule.getNextHopIp())
                            .setPriority(Integer.parseInt(internalRoutingRule.getPriority()))
                            .setOperationType(Common.OperationType.CREATE)
                            .setRoutingRuleExtraInfo(routingRuleExtraInfo)
                            .build();
                    routingRuleList.add(routingRule);
                  }
                  subnetRoutingTables2.add(
                      subnetRoutingTable.toBuilder().addAllRoutingRules(routingRuleList).build());
                }

                Router.RouterConfiguration routerConfiguration =
                    Router.RouterConfiguration.newBuilder()
                        .setFormatVersion(FORMAT_REVISION_NUMBER)
                        .setHostDvrMacAddress(
                            internalRouterInfo.getRouterConfiguration().getHostDvrMac())
                        .setId(internalRouterInfo.getRouterConfiguration().getId())
                        .setMessageType(Common.MessageType.FULL)
                        .setRevisionNumber(FORMAT_REVISION_NUMBER)
                        .addAllSubnetRoutingTables(subnetRoutingTables2)
                        .build();
                Router.RouterState routerState =
                    Router.RouterState.newBuilder().setConfiguration(routerConfiguration).build();
                routerStateList.add(routerState);
              }

              // leave a dummy security group value since for now there is no impl for sg
              SecurityGroup.SecurityGroupConfiguration securityGroupConfiguration =
                  SecurityGroup.SecurityGroupConfiguration.newBuilder().build();
              final SecurityGroup.SecurityGroupState securityGroupState =
                  SecurityGroup.SecurityGroupState.newBuilder()
                      .setConfiguration(securityGroupConfiguration)
                      .build();
              final Goalstate.GoalState goalState =
                  Goalstate.GoalState.newBuilder()
                      .addAllPortStates(portStateHashSet)
                      .addAllNeighborStates(neighborStates)
                      .addAllSubnetStates(subnetStateSet)
                      .addSecurityGroupStates(0, securityGroupState)
                      .addAllRouterStates(routerStateList)
                      .addAllDhcpStates(dhcpStateList)
                      //                      .addAllVpcStates(vpcStateSet)
                      .build();
              goalStateHashMap.put(currentGroupHostIp, goalState);
            });
    LOG.log(Level.INFO, goalStateHashMap.entrySet().toString());
    return goalStateHashMap;
  }

  private void createNeighborState(
      NetworkConfiguration networkConfiguration,
      Set<Neighbor.NeighborState> neighborStates,
      Set<String> brandNewIps,
      String ip,
      InternalPortEntity portStateWithEverythingFilledNB,
      Neighbor.NeighborType neighborType) {
    Neighbor.NeighborConfiguration.FixedIp fixedIp =
        Neighbor.NeighborConfiguration.FixedIp.newBuilder()
            .setIpAddress(ip)
            .setSubnetId(ipSubnetIdMap.get(ip))
            .setNeighborType(neighborType)
            .build();
    Neighbor.NeighborConfiguration neighborConfiguration =
        Neighbor.NeighborConfiguration.newBuilder()
            .addFixedIps(fixedIp)
            .setHostIpAddress(ipHostIpMap.get(ip))
            .setMacAddress(ipMacMap.get(ip))
            .setFormatVersion(FORMAT_REVISION_NUMBER)
            .setRevisionNumber(REVISION_NUMBER_FIELD_NUMBER)
            .setName(portStateWithEverythingFilledNB.getName())
            .setId(ipPortIdMap.get(ip))
            .setProjectId(portStateWithEverythingFilledNB.getProjectId())
            .setVpcId(
                portIdPortMap.get(ipPortIdMap.get(ip)).getVpcEntities().iterator().next().getId())
            .build();
    Common.OperationType target = null;
    if (networkConfiguration.getOpType().equals(Common.OperationType.CREATE)) {
      target = Common.OperationType.CREATE;
    }
    Neighbor.NeighborState neighborState =
        Neighbor.NeighborState.newBuilder()
            .setConfiguration(neighborConfiguration)
            .setOperationType(target)
            .build();
    neighborStates.add(neighborState);
    brandNewIps.add(ip);
  }

  /**
   * bind Host With Ports
   *
   * @param neighborInfoInSameSubenetMap same subnet neighborInfo mapping
   * @param portsInSameSubnetMap same subnet portId mapping
   * @param mapGroupedByHostIp portsList hostIp mapping
   * @param subnetMap
   * @param vpcMap
   * @param bindingHostIP
   * @param currentPortEntity
   * @param portStates
   */
  private int bindHostWithPorts(
      Map<String, Set<String>> portsInSameSubnetMap,
      Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap,
      Map<String, List<InternalPortEntity>> mapGroupedByHostIp,
      Map<String, InternalSubnetEntity> subnetMap,
      Map<String, VpcEntity> vpcMap,
      int portCounter,
      String bindingHostIP,
      InternalPortEntity currentPortEntity,
      List<InternalPortEntity> portStates,
      int type) {
    fillSubnetAndVpcToPort(
        subnetMap,
        vpcMap,
        currentPortEntity,
        portStates,
        neighborInfoInSameSubenetMap,
        portsInSameSubnetMap,
        type);
    portCounter++;
    mapGroupedByHostIp.put(bindingHostIP, portStates);
    return portCounter;
  }

  /**
   * fill all resources to ports
   *
   * @param subnetMap hostip and subnetEntity mapping
   * @param vpcMap hostIp and Vpc mapping
   * @param currentPortEntity
   * @param portStates
   * @param neighborInfoInSameSubenetMap same subnet neighborInfo mapping
   * @param portsInSameSubnetMap same subnet portId mapping
   */
  private void fillSubnetAndVpcToPort(
      Map<String, InternalSubnetEntity> subnetMap,
      Map<String, VpcEntity> vpcMap,
      InternalPortEntity currentPortEntity,
      List<InternalPortEntity> portStates,
      Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap,
      Map<String, Set<String>> portsInSameSubnetMap,
      int type) {

    Set<InternalSubnetEntity> allSubletsInOnePort = new HashSet<>();
    Set<VpcEntity> vpcEntityHashSet = new HashSet<>();

    for (com.futurewei.alcor.web.entity.port.PortEntity.FixedIp fixedIp :
        currentPortEntity.getFixedIps()) {
      String checkString = null;
      if (type == 2) {
        checkString = fixedIp.getSubnetId();
      } else if (type == 3) {
        checkString = subnetMap.get(fixedIp.getSubnetId()).getVpcId();
      }
      if (!portsInSameSubnetMap.containsKey(checkString)) {
        Set<String> tempPorts = new HashSet<>();
        Set<NeighborInfo> tempNeighbor = new HashSet<>();
        groupNeighborAndPortsBySubnet(
            currentPortEntity,
            fixedIp,
            tempPorts,
            tempNeighbor,
            neighborInfoInSameSubenetMap,
            portsInSameSubnetMap);

      } else {
        Set<String> tempPorts = portsInSameSubnetMap.get(checkString);
        Set<NeighborInfo> tempNeighbor = neighborInfoInSameSubenetMap.get(fixedIp.getSubnetId());
        groupNeighborAndPortsBySubnet(
            currentPortEntity,
            fixedIp,
            tempPorts,
            tempNeighbor,
            neighborInfoInSameSubenetMap,
            portsInSameSubnetMap);
      }

      final InternalSubnetEntity subnetInfo = subnetMap.get(fixedIp.getSubnetId());
      allSubletsInOnePort.add(subnetInfo);
      final VpcEntity vpcEntity = vpcMap.get(subnetInfo.getVpcId());
      vpcEntityHashSet.add(vpcEntity);
    }
    currentPortEntity.setSubnetEntities(allSubletsInOnePort);
    currentPortEntity.setVpcEntities(vpcEntityHashSet);
    portStates.add(currentPortEntity);
  }

  /**
   * group neighbor and ports by subnetid
   *
   * @param currentPortEntity
   * @param fixedIp
   * @param tempPorts
   * @param tempNeighbor
   * @param neighborInfoInSameSubenetMap same subnet neighborInfo mapping
   * @param portsInSameSubnetMap same subnet portId mapping
   */
  private void groupNeighborAndPortsBySubnet(
      InternalPortEntity currentPortEntity,
      PortEntity.FixedIp fixedIp,
      Set<String> tempPorts,
      Set<NeighborInfo> tempNeighbor,
      Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap,
      Map<String, Set<String>> portsInSameSubnetMap) {
    tempNeighbor.add(
        new NeighborInfo(
            currentPortEntity.getBindingHostIP(),
            currentPortEntity.getBindingHostId(),
            currentPortEntity.getId(),
            currentPortEntity.getMacAddress()));
    tempPorts.add(currentPortEntity.getId());
    /*
    if (currentPortEntity.getNeighborInfos() != null) {
      for (NeighborInfo neighborInfo : currentPortEntity.getNeighborInfos()) {
        tempNeighbor.add(
            new NeighborInfo(
                neighborInfo.getHostIp(),
                neighborInfo.getHostId(),
                neighborInfo.getPortId(),
                neighborInfo.getPortMac(),
                neighborInfo.getPortIp()));
        tempPorts.add(neighborInfo.getPortId());
      }
    }

     */

    portsInSameSubnetMap.put(fixedIp.getSubnetId(), tempPorts);
    neighborInfoInSameSubenetMap.put(fixedIp.getSubnetId(), tempNeighbor);
  }
  /**
   * deploy GoalState to ACA in parallel and return ACA processing result to upper layer
   *
   * @param gss bindHostIp realated goalstate
   * @param isFast is Fastpath
   * @param port is grpc port
   * @param isOvs is is ovs or mizar etc
   * @return List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  public List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
      talkToACA(Map<String, Goalstate.GoalState> gss, boolean isFast, int port, boolean isOvs) {
    return goalStateService.SendGoalStateToHosts(gss, isFast, port, isOvs);
  }
}
