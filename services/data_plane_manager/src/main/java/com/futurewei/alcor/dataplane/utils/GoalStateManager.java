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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.enumClass.OperationType;
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
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.logging.*;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;
import static com.futurewei.alcor.schema.Port.PortConfiguration.REVISION_NUMBER_FIELD_NUMBER;

@Component
public class GoalStateManager {
  public static final int FORMAT_REVISION_NUMBER = 1;
  @Autowired private GoalStateService goalStateService;
  private static final Logger LOG = LoggerFactory.getLogger();

  /**
   * convert and parse dpm input to dpm internal data structure
   *
   * @param networkConfiguration msg to be parsed
   * @param ipPortIdMap map of ip --> portId
   * @param ipMacMap map of ip --> Mac
   * @param ipSubnetIdMap map of ip --> SubnetId
   * @param ipHostIpMap map of ip --> HostIp
   * @param hostIpFixedIpsMap map of hostIp --> FixedIp
   * @param hostIpSubnetIdsMap map of hostIp --> SubnetId
   * @param subnetIdSubnetsMap map of subnetId --> Subnet
   * @param portIdPortMap map of portId --> Port
   * @param portIdNeighborInfoMap map of portId --> NeighborInfo
   */
  private void convert(
      NetworkConfiguration networkConfiguration,
      Map<String, String> ipPortIdMap,
      Map<String, String> ipMacMap,
      Map<String, String> ipSubnetIdMap,
      Map<String, String> ipHostIpMap,
      Map<String, Set<String>> hostIpFixedIpsMap,
      Map<String, Set<String>> hostIpSubnetIdsMap,
      Map<String, InternalSubnetEntity> subnetIdSubnetsMap,
      Map<String, InternalPortEntity> portIdPortMap,
      Map<String, NeighborInfo> portIdNeighborInfoMap) {

    final List<NeighborInfo> neighborInfos = networkConfiguration.getNeighborInfos();

    final List<InternalPortEntity> portEntities = networkConfiguration.getPortEntities();
    for (InternalSubnetEntity internalSubnetEntity : networkConfiguration.getSubnets())
      subnetIdSubnetsMap.put(internalSubnetEntity.getId(), internalSubnetEntity);

    // for newly created ports
    for (InternalPortEntity internalPortEntity : portEntities) {
      portIdPortMap.put(internalPortEntity.getId(), internalPortEntity);

      for (PortEntity.FixedIp ip : internalPortEntity.getFixedIps()) {
        Set<String> fixedIpsSet = hostIpFixedIpsMap.get(internalPortEntity.getBindingHostIP());
        if (fixedIpsSet == null) fixedIpsSet = new TreeSet<>();
        Set<String> subnetIdsSet = hostIpSubnetIdsMap.get(internalPortEntity.getBindingHostIP());
        if (subnetIdsSet == null) subnetIdsSet = new TreeSet<>();
        fixedIpsSet.add(ip.getIpAddress());
        subnetIdsSet.add(ip.getSubnetId());
        hostIpFixedIpsMap.put(internalPortEntity.getBindingHostIP(), fixedIpsSet);
        hostIpSubnetIdsMap.put(internalPortEntity.getBindingHostIP(), subnetIdsSet);

        ipPortIdMap.put(ip.getIpAddress(), internalPortEntity.getId());
        ipHostIpMap.put(ip.getIpAddress(), internalPortEntity.getBindingHostIP());
        ipSubnetIdMap.put(ip.getIpAddress(), ip.getSubnetId());
        ipMacMap.put(ip.getIpAddress(), internalPortEntity.getMacAddress());
      }
    }
    // for neighbor infos
    if (neighborInfos != null) {
      for (NeighborInfo internalPortEntity : neighborInfos) {
        ipPortIdMap.put(internalPortEntity.getPortIp(), internalPortEntity.getPortId());
        ipHostIpMap.put(internalPortEntity.getPortIp(), internalPortEntity.getHostIp());
        portIdNeighborInfoMap.put(internalPortEntity.getPortIp(), internalPortEntity);
        String portIp = internalPortEntity.getPortIp();
        Set<String> fixedIps = hostIpFixedIpsMap.get(internalPortEntity.getHostIp());
        if (fixedIps == null) fixedIps = new TreeSet<>();
        Set<String> subnetIds = hostIpSubnetIdsMap.get(internalPortEntity.getHostIp());
        if (subnetIds == null) subnetIds = new TreeSet<>();
        fixedIps.add(portIp);
        subnetIds.add(internalPortEntity.getSubnetId());
        hostIpFixedIpsMap.put(internalPortEntity.getHostIp(), fixedIps);
        hostIpSubnetIdsMap.put(internalPortEntity.getHostIp(), subnetIds);
        ipPortIdMap.put(portIp, internalPortEntity.getPortId());
        ipHostIpMap.put(portIp, internalPortEntity.getHostIp());
        ipSubnetIdMap.put(portIp, internalPortEntity.getSubnetId());
        ipMacMap.put(portIp, internalPortEntity.getPortMac());
      }
    }
  }
  /**
   * print dpm input msg
   *
   * @param networkConfiguration msg
   */
  private void printNetworkConfiguration(NetworkConfiguration networkConfiguration) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String json = objectMapper.writeValueAsString(networkConfiguration);
      LOG.log(Level.INFO, "@@@input json str: " + json);

    } catch (JsonProcessingException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
  }
  /**
   * transform client of dpm msg to aca protobuf format
   *
   * @param networkConfiguration msg to be transformmed
   * @return Map<String, Goalstate.GoalState>
   * @throws RuntimeException Various exceptions that may occur during the send process
   */
  @Async
  public Future<Map<String, Goalstate.GoalState>> transformNorthToSouth(
      NetworkConfiguration networkConfiguration) throws RuntimeException {

    Map<String, String> ipPortIdMap = new ConcurrentHashMap<>();
    Map<String, String> ipMacMap = new ConcurrentHashMap<>();
    Map<String, String> ipSubnetIdMap = new ConcurrentHashMap<>();
    Map<String, String> ipHostIpMap = new ConcurrentHashMap<>();
    Map<String, Set<String>> hostIpFixedIpsMap = new ConcurrentHashMap<>();
    Map<String, Set<String>> hostIpSubnetIdsMap = new ConcurrentHashMap<>();
    Map<String, InternalSubnetEntity> subnetIdSubnetsMap = new ConcurrentHashMap<>();
    Map<String, InternalPortEntity> portIdPortMap = new ConcurrentHashMap<>();
    Map<String, NeighborInfo> portIdNeighborInfoMap = new ConcurrentHashMap<>();

    // print entry input for debug usage
    printNetworkConfiguration(networkConfiguration);
    convert(
        networkConfiguration,
        ipPortIdMap,
        ipMacMap,
        ipSubnetIdMap,
        ipHostIpMap,
        hostIpFixedIpsMap,
        hostIpSubnetIdsMap,
        subnetIdSubnetsMap,
        portIdPortMap,
        portIdNeighborInfoMap);

    Map<String, Set<String>> portsInSameSubnetMap = new ConcurrentHashMap<>();

    Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap = new ConcurrentHashMap<>();
    // L3
    Map<String, Set<String>> portsInSameVpcMap = new ConcurrentHashMap<>();

    Map<String, Set<NeighborInfo>> neighborInfoInSameVpcMap = new ConcurrentHashMap<>();

    InternalPortEntity[] portStatesArr =
        networkConfiguration.getPortEntities().toArray(new InternalPortEntity[0]);
    InternalSubnetEntity[] subnetArr =
        networkConfiguration.getSubnets().toArray(new InternalSubnetEntity[0]);
    com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcArr =
        networkConfiguration.getVpcs().toArray(new com.futurewei.alcor.web.entity.vpc.VpcEntity[0]);

    // TODO need to v2 subnet and vpc part when logic is
    //  clear and integration done
    Map<String, List<InternalPortEntity>> mapGroupedByHostIp = new ConcurrentHashMap();
    Map<String, InternalSubnetEntity> subnetMap = new ConcurrentHashMap<>();
    Map<String, InternalPortEntity> portMap = new ConcurrentHashMap<>();
    Map<String, com.futurewei.alcor.web.entity.vpc.VpcEntity> vpcMap = new ConcurrentHashMap<>();
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
    Map<String, Goalstate.GoalState> goalStateConcurrentHashMap = new ConcurrentHashMap<>();
    // TODO would opt this part when perf needed
    hostIpSubnetIdsMap
        .keySet()
        .forEach(
            currentGroupHostIp -> {
              Set<PortState> portStateHashSet = new HashSet<>();
              Map<String, Neighbor.NeighborState> neighborStates = new ConcurrentHashMap<>();
              Set<Subnet.SubnetState> subnetStateSet = new HashSet();
              Set<Vpc.VpcState> vpcStateSet = new HashSet();
              Set<DHCP.DHCPState> dhcpStateList = new HashSet();
              final List<InternalPortEntity> internalPortEntitySet =
                  mapGroupedByHostIp.get(currentGroupHostIp);
              if (internalPortEntitySet != null) {
                internalPortEntitySet.forEach(
                    portStateWithEverythingFilledNB -> {
                      List<FixedIp> fixedIps = new ArrayList();
                      boolean isExistingPort = false;
                      final List<PortEntity.FixedIp> fixedIps1 =
                          portStateWithEverythingFilledNB.getFixedIps();
                      for (PortEntity.FixedIp fixedIp : fixedIps1) {
                        fixedIps.add(
                            FixedIp.newBuilder()
                                .setSubnetId(fixedIp.getSubnetId())
                                .setIpAddress(fixedIp.getIpAddress())
                                .build());
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
                                  .setMacAddress(portStateWithEverythingFilledNB.getMacAddress())
                                  .setIpv4Address(fixedIp.getIpAddress())
                                  .build();
                          DHCP.DHCPState dhcpState =
                              DHCP.DHCPState.newBuilder()
                                  .setConfiguration(dhcpConfiguration)
                                  .build();
                          dhcpStateList.add(dhcpState);
                          isExistingPort = true;
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
                              .setId(portStateWithEverythingFilledNB.getId())
                              .setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE)
                              .setMessageTypeValue(Common.MessageType.FULL_VALUE)
                              .build();

                      final PortState portStateSB =
                          PortState.newBuilder()
                              .setConfiguration(portConfiguration)
                              .setOperationType(Common.OperationType.CREATE)
                              .build();

                      portStateHashSet.add(portStateSB);
                    });
              }

              // avoid duplicate

              if (networkConfiguration.getNeighborTable() != null
                  && neighborStates.size() == 0
                  && networkConfiguration.getNeighborTable().size() > 0) {

                Set<String> brandNewIps = new ConcurrentSkipListSet();
                if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() > 1) {
                  for (String ip :
                      hostIpFixedIpsMap.values().stream()
                          .flatMap(alist -> alist.stream())
                          .collect(Collectors.toList())) {
                    {
                      Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                      Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                      if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                        if (!hostIpSubnetIdsMap
                            .get(currentGroupHostIp)
                            .iterator()
                            .next()
                            .equals(ipSubnetIdMap.get(ip))) {
                          l2 = Neighbor.NeighborType.L3;
                          l3 = Neighbor.NeighborType.L2;
                        }
                      }

                      if (!portIdNeighborInfoMap.containsKey(ip)) {
                        createNeighborState(
                            networkConfiguration,
                            neighborStates,
                            brandNewIps,
                            ip,
                            currentGroupHostIp,
                            l3,
                            ipPortIdMap,
                            ipMacMap,
                            ipSubnetIdMap,
                            ipHostIpMap,
                            hostIpFixedIpsMap,
                            hostIpSubnetIdsMap,
                            subnetIdSubnetsMap,
                            portIdPortMap,
                            portIdNeighborInfoMap);
                      } // if contains ip in neighbor info
                    }
                  }
                } else if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                  for (String ip :
                      hostIpFixedIpsMap.values().stream()
                          .flatMap(alist -> alist.stream())
                          .collect(Collectors.toList())) {
                    Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                    Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                    if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                      if (!hostIpSubnetIdsMap
                          .get(currentGroupHostIp)
                          .iterator()
                          .next()
                          .equals(ipSubnetIdMap.get(ip))) {
                        l2 = Neighbor.NeighborType.L3;
                        l3 = Neighbor.NeighborType.L2;
                      }
                    }

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
                            currentGroupHostIp,
                            l2,
                            ipPortIdMap,
                            ipMacMap,
                            ipSubnetIdMap,
                            ipHostIpMap,
                            hostIpFixedIpsMap,
                            hostIpSubnetIdsMap,
                            subnetIdSubnetsMap,
                            portIdPortMap,
                            portIdNeighborInfoMap);
                      } else {
                        createNeighborState(
                            networkConfiguration,
                            neighborStates,
                            brandNewIps,
                            ip,
                            currentGroupHostIp,
                            l3,
                            ipPortIdMap,
                            ipMacMap,
                            ipSubnetIdMap,
                            ipHostIpMap,
                            hostIpFixedIpsMap,
                            hostIpSubnetIdsMap,
                            subnetIdSubnetsMap,
                            portIdPortMap,
                            portIdNeighborInfoMap);
                      } // end if internal size==1 sn>2
                      brandNewIps.add(ip);
                    }
                  }
                  // start new loop for adding new ip to existing port

                } // size ==1
                for (String nip : brandNewIps) {
                  if (!ipHostIpMap.get(nip).equals(currentGroupHostIp)) continue;
                  for (String eip : portIdNeighborInfoMap.keySet()) {

                    Neighbor.NeighborType l3 = Neighbor.NeighborType.L3;
                    Neighbor.NeighborType l2 = Neighbor.NeighborType.L2;

                    if (hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1) {
                      if (!hostIpSubnetIdsMap
                          .get(currentGroupHostIp)
                          .iterator()
                          .next()
                          .equals(ipSubnetIdMap.get(nip))) {
                        l2 = Neighbor.NeighborType.L3;
                        l3 = Neighbor.NeighborType.L2;
                      }
                    }

                    if (ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) { // ip 2.2
                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          currentGroupHostIp,
                          l3,
                          ipPortIdMap,
                          ipMacMap,
                          ipSubnetIdMap,
                          ipHostIpMap,
                          hostIpFixedIpsMap,
                          hostIpSubnetIdsMap,
                          subnetIdSubnetsMap,
                          portIdPortMap,
                          portIdNeighborInfoMap);
                    } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (!ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {
                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          currentGroupHostIp,
                          l3,
                          ipPortIdMap,
                          ipMacMap,
                          ipSubnetIdMap,
                          ipHostIpMap,
                          hostIpFixedIpsMap,
                          hostIpSubnetIdsMap,
                          subnetIdSubnetsMap,
                          portIdPortMap,
                          portIdNeighborInfoMap);
                    } else if (!ipHostIpMap.get(eip).equals(currentGroupHostIp)
                        && (ipSubnetIdMap.get(eip).equals(ipSubnetIdMap.get(nip)))) {

                      createNeighborState(
                          networkConfiguration,
                          neighborStates,
                          brandNewIps,
                          eip,
                          currentGroupHostIp,
                          l2,
                          ipPortIdMap,
                          ipMacMap,
                          ipSubnetIdMap,
                          ipHostIpMap,
                          hostIpFixedIpsMap,
                          hostIpSubnetIdsMap,
                          subnetIdSubnetsMap,
                          portIdPortMap,
                          portIdNeighborInfoMap);
                    } // inner if end
                  } // 2nd loop end
                } // current nip end
              } // if size==0

              // lookup subnet entity
              for (String sid : ipSubnetIdMap.values()) {
                InternalSubnetEntity subnetEntity1 = subnetIdSubnetsMap.get(sid);
                if (subnetEntity1 == null) {
                  LOG.log(Level.SEVERE, sid + "subnet is MISSING");
                  continue;
                }
                add2SubnetStates(networkConfiguration, subnetStateSet, subnetEntity1);
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
              if (networkConfiguration.getInternalRouterInfos() != null
                  && neighborStates.keySet().stream().filter(e -> e.indexOf("#L3") != -1).count()
                      > 0) {
                for (InternalRouterInfo internalRouterInfo :
                    networkConfiguration.getInternalRouterInfos()) {
                  final List<InternalSubnetRoutingTable> subnetRoutingTables =
                      internalRouterInfo.getRouterConfiguration().getSubnetRoutingTables();
                  final List<Router.RouterConfiguration.SubnetRoutingTable> subnetRoutingTables2 =
                      new ArrayList<>();
                  for (InternalSubnetRoutingTable internalSubnetRoutingTable :
                      subnetRoutingTables) {
                    if (neighborStates.keySet().stream().filter(e -> e.indexOf("#L3") != -1).count()
                        > 0) {
                      final InternalSubnetEntity subnetEntity1 =
                          subnetIdSubnetsMap.get(internalSubnetRoutingTable.getSubnetId());

                      add2SubnetStates(networkConfiguration, subnetStateSet, subnetEntity1);
                    }

                    Router.RouterConfiguration.SubnetRoutingTable subnetRoutingTable =
                        Router.RouterConfiguration.SubnetRoutingTable.newBuilder()
                            .setSubnetId(internalSubnetRoutingTable.getSubnetId())
                            .buildPartial();
                    List<Router.RouterConfiguration.RoutingRule> routingRuleList =
                        new ArrayList<>();
                    for (InternalRoutingRule internalRoutingRule :
                        internalSubnetRoutingTable.getRoutingRules()) {
                      Router.DestinationType destinationType = Router.DestinationType.INTERNET;
                      Router.RouterConfiguration.RoutingRuleExtraInfo routingRuleExtraInfo =
                          Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder()
                              .setDestinationType(destinationType)
                              .setNextHopMac(
                                  internalRoutingRule.getRoutingRuleExtraInfo().getNextHopMac())
                              .build();
                      Common.OperationType op =
                          getOperationType(
                              internalRoutingRule.getOperationType().equals(OperationType.CREATE),
                              internalRoutingRule.getOperationType().equals(OperationType.INFO),
                              Common.OperationType.INFO,
                              internalRoutingRule.getOperationType().equals(OperationType.DELETE),
                              Common.OperationType.DELETE,
                              internalRoutingRule.getOperationType().equals(OperationType.UPDATE),
                              Common.OperationType.UPDATE);

                      Router.RouterConfiguration.RoutingRule routingRule =
                          Router.RouterConfiguration.RoutingRule.newBuilder()
                              .setDestination(internalRoutingRule.getDestination())
                              .setId(internalRoutingRule.getId())
                              .setName(internalRoutingRule.getName())
                              .setNextHopIp(internalRoutingRule.getNextHopIp())
                              .setPriority(Integer.parseInt(internalRoutingRule.getPriority()))
                              .setOperationType(op)
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
                      .addAllNeighborStates(neighborStates.values())
                      .addAllSubnetStates(subnetStateSet)
                      .addSecurityGroupStates(0, securityGroupState)
                      .addAllRouterStates(routerStateList)
                      .addAllDhcpStates(dhcpStateList)
                      .build();
              goalStateConcurrentHashMap.put(currentGroupHostIp, goalState);
            });
    LOG.log(Level.INFO, goalStateConcurrentHashMap.entrySet().toString());
    return new AsyncResult<Map<String, Goalstate.GoalState>>(goalStateConcurrentHashMap);
  }

  private void add2SubnetStates(
      NetworkConfiguration networkConfiguration,
      Set<Subnet.SubnetState> subnetStateSet,
      InternalSubnetEntity subnetEntity1) {
    Subnet.SubnetConfiguration.Gateway gateway =
        Subnet.SubnetConfiguration.Gateway.newBuilder()
            .setIpAddress(subnetEntity1.getGatewayIp())
            .setMacAddress(subnetEntity1.getGatewayMacAddress())
            .build();
    Subnet.SubnetConfiguration subnetConfiguration =
        Subnet.SubnetConfiguration.newBuilder()
            .setId(subnetEntity1.getId())
            .setVpcId(subnetEntity1.getVpcId())
            .setProjectId(subnetEntity1.getProjectId())
            .setCidr(subnetEntity1.getCidr())
            .setTunnelId(subnetEntity1.getTunnelId())
            .setGateway(gateway)
            .setFormatVersion(FORMAT_REVISION_NUMBER)
            .build();
    Subnet.SubnetState subnetState =
        Subnet.SubnetState.newBuilder().setConfiguration(subnetConfiguration).buildPartial();
    if (networkConfiguration.getRsType().equals(Common.ResourceType.PORT))
      subnetState = subnetState.toBuilder().setOperationType(Common.OperationType.INFO).build();
    else
      subnetState = subnetState.toBuilder().setOperationType(Common.OperationType.CREATE).build();

    subnetStateSet.add(subnetState);
  }

  @Async
  /**
   * create L3/L2 neighbors
   *
   * @param networkConfiguration msg to be parsed
   * @param ipPortIdMap map of ip --> portId
   * @param ipMacMap map of ip --> Mac
   * @param ipSubnetIdMap map of ip --> SubnetId
   * @param ipHostIpMap map of ip --> HostIp
   * @param hostIpFixedIpsMap map of hostIp --> FixedIp
   * @param hostIpSubnetIdsMap map of hostIp --> SubnetId
   * @param subnetIdSubnetsMap map of subnetId --> Subnet
   * @param portIdPortMap map of portId --> Port
   * @param portIdNeighborInfoMap map of portId --> NeighborInfo
   */
  private void createNeighborState(
      NetworkConfiguration networkConfiguration,
      Map<String, Neighbor.NeighborState> neighborStates,
      Set<String> brandNewIps,
      String ip,
      String currentGroupHostIp,
      Neighbor.NeighborType neighborType,
      Map<String, String> ipPortIdMap,
      Map<String, String> ipMacMap,
      Map<String, String> ipSubnetIdMap,
      Map<String, String> ipHostIpMap,
      Map<String, Set<String>> hostIpFixedIpsMap,
      Map<String, Set<String>> hostIpSubnetIdsMap,
      Map<String, InternalSubnetEntity> subnetIdSubnetsMap,
      Map<String, InternalPortEntity> portIdPortMap,
      Map<String, NeighborInfo> portIdNeighborInfoMap) {

    // corner case check
    if (ipHostIpMap.get(ip).equals(currentGroupHostIp)) {
      for (InternalPortEntity it : networkConfiguration.getPortEntities()) {
        for (PortEntity.FixedIp f : it.getFixedIps()) {
          if (f.getIpAddress().equals(ip) && hostIpSubnetIdsMap.get(currentGroupHostIp).size() == 1)
            return;
        }
      }
    }
    // corner case check
    if (networkConfiguration.getNeighborTable() == null
        && networkConfiguration.getNeighborInfos() == null) return;
    if (currentGroupHostIp.equals(ipHostIpMap.get(ip))
        && networkConfiguration.getNeighborInfos().size()
            == networkConfiguration.getNeighborTable().size()
        && networkConfiguration.getNeighborTable().size() > 1) {
      Set<String> hostIps = new HashSet();
      for (NeighborEntry n : networkConfiguration.getNeighborTable()) {
        hostIps.add(ipHostIpMap.get(n.getNeighborIp()));
      }
      if (hostIps.size() == 1)
        for (InternalPortEntity i : portIdPortMap.values()) {
          for (PortEntity.FixedIp fixedIp : i.getFixedIps()) {
            if (fixedIp.getIpAddress().equals(ip)) return;
          }
        }
    }
    // construct the L3/L2 neighbor
    if (neighborStates.containsKey(ip + "#" + Neighbor.NeighborType.L3)) return;
    else if ((neighborStates.containsKey(ip + "#" + Neighbor.NeighborType.L2))
        && (neighborType.equals(Neighbor.NeighborType.L3)))
      neighborStates.remove(ip + "#" + Neighbor.NeighborType.L2);
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
            .setId(ipPortIdMap.get(ip))
            .setProjectId(networkConfiguration.getVpcs().get(0).getProjectId())
            .setVpcId(networkConfiguration.getVpcs().get(0).getId())
            .build();
    Common.OperationType target =
        getOperationType(
            networkConfiguration.getOpType().equals(Common.OperationType.CREATE),
            networkConfiguration.getOpType().equals(Common.OperationType.DELETE),
            Common.OperationType.DELETE,
            networkConfiguration.getOpType().equals(Common.OperationType.UPDATE),
            Common.OperationType.UPDATE,
            networkConfiguration.getOpType().equals(Common.OperationType.INFO),
            Common.OperationType.INFO);
    Neighbor.NeighborState neighborState =
        Neighbor.NeighborState.newBuilder()
            .setConfiguration(neighborConfiguration)
            .setOperationType(target)
            .build();
    neighborStates.put(ip + "#" + neighborType, neighborState);
    brandNewIps.add(ip);
  }

  @Nullable
  private Common.OperationType getOperationType(
      boolean equals,
      boolean equals2,
      Common.OperationType delete,
      boolean equals3,
      Common.OperationType update,
      boolean equals4,
      Common.OperationType info) {
    Common.OperationType target = null;
    if (equals) {
      target = Common.OperationType.CREATE;
    } else if (equals2) {
      target = delete;
    } else if (equals3) {
      target = update;
    } else if (equals4) {
      target = info;
    }
    return target;
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
  @Async
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
  @Async
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
