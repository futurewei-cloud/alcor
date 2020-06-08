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

import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.dataplane.service.impl.OVSGoalStateServiceImpl;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;

public class GoalStateUtil {

  public static Map<String, Goalstate.GoalState> transformNorthToSouth(
      NetworkConfiguration networkConfiguration) {
    com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB[] portStatesArr =
        networkConfiguration.getPortStates();
    com.futurewei.alcor.web.entity.subnet.SubnetEntity[] subnetArr =
        networkConfiguration.getSubnets();
    com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcArr = networkConfiguration.getVpcs();

    // TODO need to refactor subnet and vpc part when logic is clear and integration done
    Map<String, Set<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB>>
        mapGroupedByHostIp = new HashMap();
    Map<String, com.futurewei.alcor.web.entity.subnet.SubnetEntity> subnetMap = new HashMap<>();
    Map<String, com.futurewei.alcor.web.entity.vpc.VpcEntity> vpcMap = new HashMap<>();
    // construct map
    for (SubnetEntity s : subnetArr) {
      subnetMap.put(s.getId(), s);
    }

    for (VpcEntity vpc : vpcArr) {
      vpcMap.put(vpc.getId(), vpc);
    }
    // group nb msg by ip
    int portCounter = 0;
    for (InternalPortEntityNB portEntityNB : portStatesArr) {
      String bindingHostIP = portEntityNB.getBindingHostIP();
      InternalPortEntityNB currentPortEntity = portStatesArr[portCounter];
      if (!mapGroupedByHostIp.containsKey(bindingHostIP)) {
        Set<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB> portStates =
            new HashSet<>();
        fillSubnetAndVpcToPort(subnetMap, vpcMap, currentPortEntity, portStates);
        portCounter++;
        mapGroupedByHostIp.put(bindingHostIP, portStates);
      } else {
        Set<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB> portStates =
            mapGroupedByHostIp.get(bindingHostIP);
        fillSubnetAndVpcToPort(subnetMap, vpcMap, currentPortEntity, portStates);
        portCounter++;
        mapGroupedByHostIp.put(bindingHostIP, portStates);
      }
    }
    // construct sb msg by ip
    Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
    // TODO would opt this part when perf needed
    mapGroupedByHostIp.entrySet().stream()
        .forEach(
            f -> {
              Set<PortState> portStateHashSet = new HashSet<>();
              Set<Subnet.SubnetState> subnetStateSet = new HashSet();
              Set<Vpc.VpcState> vpcStateSet = new HashSet();
              final Set<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB>
                  internalPortEntityNBSet = f.getValue();
              internalPortEntityNBSet.stream()
                  .forEach(
                      e -> {
                        Port.PortConfiguration.HostInfo hostinfo = null;
                        if (e.getNeighborIp() != null)
                          hostinfo =
                              Port.PortConfiguration.HostInfo.newBuilder()
                                  .setIpAddress(e.getNeighborIp())
                                  .build();
                        List<FixedIp> fixedIps = new ArrayList();
                        for (PortEntity.FixedIp fixedIp : e.getFixedIps()) {
                          FixedIp fixedIp1 =
                              FixedIp.newBuilder()
                                  .setIpAddress(fixedIp.getIpAddress())
                                  .setSubnetId(fixedIp.getSubnetId())
                                  .build();
                          fixedIps.add(fixedIp1);
                        }

                        Port.PortConfiguration portConfiguration =
                            Port.PortConfiguration.newBuilder()
                                .setId(e.getId())
                                .setName(e.getName())
                                .setProjectId(e.getProjectId())
                                .setFormatVersion(1)
                                .setAdminStateUp(true)
                                .setMacAddress(e.getMacAddress())
                                .setRevisionNumber(1)
                                .addAllFixedIps(fixedIps)
                                .buildPartial();
                        if (hostinfo != null)
                          portConfiguration =
                              portConfiguration
                                  .toBuilder()
                                  .setNetworkTypeValue(
                                      Common.OperationType.NEIGHBOR_CREATE_UPDATE_VALUE)
                                  .setHostInfo(hostinfo)
                                  .setMessageTypeValue(Port.MessageType.DELTA_VALUE)
                                  .build();
                        else
                          portConfiguration =
                              portConfiguration
                                  .toBuilder()
                                  .setNetworkTypeValue(Common.OperationType.CREATE_VALUE)
                                  .setMessageTypeValue(Port.MessageType.FULL_VALUE)
                                  .build();

                        final PortState portStateSB =
                            PortState.newBuilder().setConfiguration(portConfiguration).build();
                        // lookup subnet entity
                        for (SubnetEntity subnetEntity : e.getSubnetEntities()) {
                          Subnet.SubnetConfiguration subnetConfiguration =
                              Subnet.SubnetConfiguration.newBuilder()
                                  .setId(subnetEntity.getId())
                                  .setVpcId(subnetEntity.getVpcId())
                                  .setProjectId(e.getProjectId())
                                  .setFormatVersion(1)
                                  .setTunnelId(subnetEntity.getTunnelId())
                                  .build();
                          Subnet.SubnetState subnetState =
                              Subnet.SubnetState.newBuilder()
                                  .setConfiguration(subnetConfiguration)
                                  .build();
                          subnetStateSet.add(subnetState);
                          // lookup vpc entity
                          final VpcEntity vpcEntity = vpcMap.get(subnetEntity.getVpcId());
                          Vpc.VpcConfiguration vpcConfiguration =
                              Vpc.VpcConfiguration.newBuilder()
                                  .setId(vpcEntity.getId())
                                  .setCidr(vpcEntity.getCidr())
                                  .setFormatVersion(1)
                                  .setRevisionNumber(1)
                                  .build();
                          Vpc.VpcState vpcState =
                              Vpc.VpcState.newBuilder().setConfiguration(vpcConfiguration).build();
                          vpcStateSet.add(vpcState);
                        }
                        portStateHashSet.add(portStateSB);
                      });
              //leave a dummy sg value since for now there is no impl for sg
              SecurityGroup.SecurityGroupConfiguration securityGroupConfiguration =
                  SecurityGroup.SecurityGroupConfiguration.newBuilder().build();
              final SecurityGroup.SecurityGroupState securityGroupState =
                  SecurityGroup.SecurityGroupState.newBuilder()
                      .setConfiguration(securityGroupConfiguration)
                      .build();
              final Goalstate.GoalState goalState =
                  Goalstate.GoalState.newBuilder()
                      .addAllPortStates(portStateHashSet)
                      .addAllSubnetStates(subnetStateSet)
                      .addAllVpcStates(vpcStateSet)
                      .addSecurityGroupStates(0, securityGroupState)
                      .build();
              goalStateHashMap.put(f.getKey(), goalState);
            });

    return goalStateHashMap;
  }

  private static void fillSubnetAndVpcToPort(
      Map<String, SubnetEntity> subnetMap,
      Map<String, VpcEntity> vpcMap,
      InternalPortEntityNB currentPortEntity,
      Set<InternalPortEntityNB> portStates) {
    Set<SubnetEntity> subnetEntityHashSet = new HashSet<>();
    Set<VpcEntity> vpcEntityHashSet = new HashSet<>();

    for (com.futurewei.alcor.web.entity.port.PortEntity.FixedIp fixedIp :
        currentPortEntity.getFixedIps()) {
      final SubnetEntity subnetEntity = subnetMap.get(fixedIp.getSubnetId());
      subnetEntityHashSet.add(subnetEntity);
      final VpcEntity vpcEntity = vpcMap.get(subnetEntity.getVpcId());
      vpcEntityHashSet.add(vpcEntity);
    }
    currentPortEntity.setSubnetEntities(subnetEntityHashSet);
    currentPortEntity.setVpcEntities(vpcEntityHashSet);
    portStates.add(currentPortEntity);
  }

  public static List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
      talkToACA(Map<String, Goalstate.GoalState> gss, boolean isFast) {
    // if Config.isOVS
    GoalStateService goalStateService = new OVSGoalStateServiceImpl();

    return gss.entrySet()
        .parallelStream()
        .map(
            e -> {
              goalStateService.setIp(e.getKey());
              goalStateService.setGoalState(e.getValue());
              goalStateService.setFastPath(isFast);
              return goalStateService.SendGoalStateToHosts();
            })
        .collect(Collectors.toList());
  }
}
