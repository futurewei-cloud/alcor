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
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntityNB;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;

public class GoalStateUtil {
  private static final Logger LOG = LoggerFactory.getLogger(GoalStateUtil.class);

  public Map<String, Goalstate.GoalState> transformNorthToSouth(
      NetworkConfiguration networkConfiguration) {
    com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB[] portStatesArr =
        networkConfiguration.getPortStates();
    InternalSubnetEntityNB[] subnetArr = networkConfiguration.getSubnets();
    com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcArr = networkConfiguration.getVpcs();

    // TODO need to refactor subnet and vpc part when logic is clear and integration done
    Map<String, List<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB>>
        mapGroupedByHostIp = new HashMap();
    Map<String, InternalSubnetEntityNB> subnetMap = new HashMap<>();
    Map<String, com.futurewei.alcor.web.entity.vpc.VpcEntity> vpcMap = new HashMap<>();
    // construct map
    for (InternalSubnetEntityNB s : subnetArr) {
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
        List<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB> portStates =
            new ArrayList<>();
        fillSubnetAndVpcToPort(subnetMap, vpcMap, currentPortEntity, portStates);
        portCounter++;
        mapGroupedByHostIp.put(bindingHostIP, portStates);
      } else {
        List<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB> portStates =
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
              final List<com.futurewei.alcor.web.entity.dataplane.InternalPortEntityNB>
                  internalPortEntityNBSet = f.getValue();
              internalPortEntityNBSet.stream()
                  .forEach(
                      e -> {
                        Set<Port.PortConfiguration.HostInfo> hostinfo = null;
                        if (e.getNeighborIps() != null) {
                          hostinfo = new HashSet();
                          for (NeighborInfo neighborInfo : e.getNeighborIps()) {
                            Port.PortConfiguration.HostInfo build =
                                Port.PortConfiguration.HostInfo.newBuilder()
                                    .setIpAddress(neighborInfo.getHostIp())
                                    .setMacAddress(neighborInfo.getPortId())
                                    .build();
                            hostinfo.add(build);
                          }
                        }
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
                                .setName(e.getName())
                                .setProjectId(e.getProjectId())
                                .setVpcId(e.getSubnetEntities().iterator().next().getVpcId())
                                .setFormatVersion(1)
                                .setAdminStateUp(true)
                                .setMacAddress(e.getMacAddress())
                                .setRevisionNumber(1)
                                .addAllFixedIps(fixedIps)
                                .buildPartial();
                        if (hostinfo != null) {
                          for (Port.PortConfiguration.HostInfo h : hostinfo) {
                            portConfiguration =
                                portConfiguration
                                    .toBuilder()
                                    .setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE)
                                    .setHostInfo(h)
                                    .setId(h.getMacAddress())
                                    .setMessageTypeValue(Port.MessageType.DELTA_VALUE)
                                    .build();
                            final PortState portStateSB =
                                PortState.newBuilder()
                                    .setConfiguration(portConfiguration)
                                    .setOperationType(Common.OperationType.NEIGHBOR_CREATE_UPDATE)
                                    .build();
                            portStateHashSet.add(portStateSB);
                          }
                        } else {
                          portConfiguration =
                              portConfiguration
                                  .toBuilder()
                                  .setId(e.getId())
                                  .setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE)
                                  .setMessageTypeValue(Port.MessageType.FULL_VALUE)
                                  .build();

                          final PortState portStateSB =
                              PortState.newBuilder()
                                  .setConfiguration(portConfiguration)
                                  .setOperationType(Common.OperationType.CREATE)
                                  .build();
                          portStateHashSet.add(portStateSB);
                        }

                        // lookup subnet entity
                        for (InternalSubnetEntityNB subnetEntity1 : e.getSubnetEntities()) {
                          Subnet.SubnetConfiguration subnetConfiguration =
                              Subnet.SubnetConfiguration.newBuilder()
                                  .setId(subnetEntity1.getId())
                                  .setVpcId(subnetEntity1.getVpcId())
                                  .setProjectId(e.getProjectId())
                                  .setCidr(subnetEntity1.getCidr())
                                  .setFormatVersion(1)
                                  .setTunnelId(subnetEntity1.getTunnelId())
                                  .build();
                          Subnet.SubnetState subnetState =
                              Subnet.SubnetState.newBuilder()
                                  .setConfiguration(subnetConfiguration)
                                  .buildPartial();
                          if (networkConfiguration.getOpType().equals(Common.OperationType.CREATE)
                              && networkConfiguration.getRsType().equals(Common.ResourceType.PORT))
                            subnetState =
                                subnetState
                                    .toBuilder()
                                    .setOperationType(Common.OperationType.INFO)
                                    .build();
                          else
                            subnetState =
                                subnetState
                                    .toBuilder()
                                    .setOperationType(Common.OperationType.CREATE)
                                    .build();

                          subnetStateSet.add(subnetState);
                            // lookup vpc entity
                            final VpcEntity vpcEntity = vpcMap.get(subnetEntity1.getVpcId());
                            Vpc.VpcConfiguration vpcConfiguration =
                                Vpc.VpcConfiguration.newBuilder()
                                    .setId(vpcEntity.getId())
                                    .setCidr(vpcEntity.getCidr())
                                    .setFormatVersion(1)
                                    .setRevisionNumber(1)
                                    .build();
                            Vpc.VpcState vpcState =
                                Vpc.VpcState.newBuilder()
                                    .setConfiguration(vpcConfiguration)
                                    .setOperationTypeValue(Common.OperationType.CREATE_VALUE)
                                    .setOperationType(Common.OperationType.CREATE)
                                    .build();
                            vpcStateSet.add(vpcState);
                        }
                      });
              // leave a dummy sg value since for now there is no impl for sg
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
                      .addSecurityGroupStates(0, securityGroupState)
                      //                          .addAllVpcStates(vpcStateSet)
                      .build();
              goalStateHashMap.put(f.getKey(), goalState);
            });
    LOG.info(goalStateHashMap.entrySet().toString());
    return goalStateHashMap;
  }

  private void fillSubnetAndVpcToPort(
      Map<String, InternalSubnetEntityNB> subnetMap,
      Map<String, VpcEntity> vpcMap,
      InternalPortEntityNB currentPortEntity,
      List<InternalPortEntityNB> portStates) {
    Set<InternalSubnetEntityNB> subnetEntity1HashSet = new HashSet<>();
    Set<VpcEntity> vpcEntityHashSet = new HashSet<>();

    for (com.futurewei.alcor.web.entity.port.PortEntity.FixedIp fixedIp :
        currentPortEntity.getFixedIps()) {
      final InternalSubnetEntityNB subnetEntity1 = subnetMap.get(fixedIp.getSubnetId());
      subnetEntity1HashSet.add(subnetEntity1);
      final VpcEntity vpcEntity = vpcMap.get(subnetEntity1.getVpcId());
      vpcEntityHashSet.add(vpcEntity);
    }
    currentPortEntity.setSubnetEntities(subnetEntity1HashSet);
    currentPortEntity.setVpcEntities(vpcEntityHashSet);
    portStates.add(currentPortEntity);
  }

  ExecutorService executorService = Executors.newCachedThreadPool();

  public List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
      talkToACA(Map<String, Goalstate.GoalState> gss, boolean isFast, int port, boolean isOvs) {
    if (isOvs) {
      List<List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result =
          new ArrayList<>();

      gss.entrySet().parallelStream()
          .map(
              e -> {
                return executorService.submit(
                    () -> {
                      GoalStateService goalStateService = new OVSGoalStateServiceImpl();
                      goalStateService.setIp(e.getKey());
                      goalStateService.setGoalState(e.getValue());
                      goalStateService.setFastPath(isFast);
                      goalStateService.setPort(port);
                      return goalStateService.SendGoalStateToHosts();
                    });
              })
          .collect(Collectors.toList()).stream()
          .forEach(
              e -> {
                try {
                  result.add(e.get());
                } catch (InterruptedException ex) {
                  ex.printStackTrace();
                } catch (ExecutionException ex) {
                  ex.printStackTrace();
                }
              });
      return result;
    }
    throw new RuntimeException("protocol other than ovs is not supported for now");
  }
}
