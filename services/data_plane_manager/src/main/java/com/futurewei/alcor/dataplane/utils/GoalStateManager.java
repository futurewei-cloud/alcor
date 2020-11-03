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

import com.futurewei.alcor.dataplane.exception.ClientOfDPMFailureException;
import com.futurewei.alcor.dataplane.exception.DPMFailureException;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;


import java.util.*;
import java.util.logging.Level;

import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;

@Component
public class GoalStateManager {
    public static final int FORMAT_REVISION_NUMBER = 1;
    @Autowired private GoalStateService goalStateService;
    private static final Logger LOG = LoggerFactory.getLogger();

    private void printNetworkConfiguration(NetworkConfiguration networkConfiguration)
    {
        LOG.log(Level.INFO,
                "### networkConf str: "+networkConfiguration.toString());
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
        LOG.log(Level.INFO,"###############");
        LOG.log(Level.INFO,gson.toJson(networkConfiguration));
    }
    /**
     * transform client of dpm msg to aca protobuf format
     *
     * @param networkConfiguration  msg to be transformmed
     * @return Map<String, Goalstate.GoalState>
     * @throws RuntimeException Various exceptions that may occur during the send process
     */
    public Map<String, Goalstate.GoalState> transformNorthToSouth(
            NetworkConfiguration networkConfiguration) throws RuntimeException {
        // print entry input
        printNetworkConfiguration(networkConfiguration);

        Map<String, Set<String>> portsInSameSubnetMap = new HashMap<>();

        Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap = new HashMap<>();

        InternalPortEntity[] portStatesArr =
                networkConfiguration.getPortEntities().toArray(new InternalPortEntity[0]);
        InternalSubnetEntity[] subnetArr =
                networkConfiguration.getSubnets().toArray(new InternalSubnetEntity[0]);
        com.futurewei.alcor.web.entity.vpc.VpcEntity[] vpcArr =
                networkConfiguration.getVpcs().toArray(new com.futurewei.alcor.web.entity.vpc.VpcEntity[0]);

        // TODO need to v2 subnet and vpc part when logic is
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
                portCounter = bindHostWithPorts(portsInSameSubnetMap,
                        neighborInfoInSameSubenetMap, mapGroupedByHostIp, subnetMap,
                        vpcMap, portCounter, bindingHostIP, currentPortEntity,
                        portStates);
            } else {
                List<InternalPortEntity> portStates = mapGroupedByHostIp.get(bindingHostIP);
                portCounter = bindHostWithPorts(portsInSameSubnetMap, neighborInfoInSameSubenetMap, mapGroupedByHostIp, subnetMap, vpcMap, portCounter, bindingHostIP, currentPortEntity, portStates);
            }
        }

        portsInSameSubnetMap.keySet().stream()
                .forEach(
                        sid -> {
                            for (String pid : portsInSameSubnetMap.get(sid)) {
                                final Set<NeighborInfo> neighborInfos = neighborInfoInSameSubenetMap.get(sid);
                                final InternalPortEntity internalPortEntity = portMap.get(pid);
                                if (internalPortEntity == null) {
                                    LOG.log(Level.WARNING, (
                                            "portId: "
                                                    + pid
                                                    + " provided in neighbor but NOT in port_internal, skip for now, likely to be dpm client error"));
                                    continue;
                                }
                                try {
                                    final Set<NeighborInfo> neighborInfos3 = new HashSet<>();
                                    for (NeighborInfo n : neighborInfos) {
                                        if (!n.getHostIp().equals(internalPortEntity.getBindingHostIP()))
                                            neighborInfos3.add(n);
                                    }

                                    if (internalPortEntity.getInternalNeighborInfo1() == null
                                            || internalPortEntity.getInternalNeighborInfo1().isEmpty()) {
                                        final Set<NeighborInfo> neighborInfos2 = new HashSet<>();
                                        neighborInfos2.addAll(neighborInfos3);
                                        internalPortEntity.setInternalNeighborInfo1(neighborInfos2);
                                    } else {
                                        final Set<NeighborInfo> neighborInfos1 =
                                                internalPortEntity.getInternalNeighborInfo1();
                                        neighborInfos1.addAll(neighborInfos3);
                                        internalPortEntity.setInternalNeighborInfo1(neighborInfos);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LOG.log(Level.WARNING,
                                            e.getMessage());
                                    throw new DPMFailureException(e.getMessage());
                                }
                            }
                        });

        // construct sb msg by ip
        Map<String, Goalstate.GoalState> goalStateHashMap = new HashMap<>();
        // TODO would opt this part when perf needed
        mapGroupedByHostIp.entrySet().stream()
                .forEach(
                        eachGSOnSingleIP -> {
                            Set<PortState> portStateHashSet = new HashSet<>();
                            Set<Subnet.SubnetState> subnetStateSet = new HashSet();
                            Set<Vpc.VpcState> vpcStateSet = new HashSet();
                            List<DHCP.DHCPState> dhcpStateList = new ArrayList();

                            final List<InternalPortEntity> internalPortEntitySet = eachGSOnSingleIP.getValue();
                            boolean m = false;
                            internalPortEntitySet.stream()
                                    .forEach(
                                            portStateWithEverythingFilledNB -> {
                                                Set<Port.PortConfiguration.HostInfo> neighborSB = new HashSet();
                                                if (portStateWithEverythingFilledNB.getInternalNeighborInfo1() != null) {
                                                    neighborSB = new HashSet();
                                                    for (NeighborInfo neighborInfo :
                                                            portStateWithEverythingFilledNB.getInternalNeighborInfo1()) {
                                                        Port.PortConfiguration.HostInfo build =
                                                                Port.PortConfiguration.HostInfo.newBuilder()
                                                                        .setIpAddress(neighborInfo.getHostIp())
                                                                        .setMacAddress(neighborInfo.getPortId())
                                                                        .build();
                                                        neighborSB.add(build);
                                                    }
                                                }
                                                List<FixedIp> fixedIps = new ArrayList();

                                                for (PortEntity.FixedIp fixedIp :
                                                        portStateWithEverythingFilledNB.getFixedIps()) {
                                                    FixedIp fixedIp1 =
                                                            FixedIp.newBuilder()
                                                                    .setIpAddress(fixedIp.getIpAddress())
                                                                    .setSubnetId(fixedIp.getSubnetId())
                                                                    .build();
                                                    fixedIps.add(fixedIp1);
                                                    DHCP.DHCPConfiguration dhcpConfiguration=DHCP.DHCPConfiguration.newBuilder()
                                                            .setRevisionNumber(FORMAT_REVISION_NUMBER)
                                                            .setFormatVersion(FORMAT_REVISION_NUMBER)
                                                            .setSubnetId(fixedIp.getSubnetId())
                                                            .setMacAddress(portStateWithEverythingFilledNB.getMacAddress())
                                                            .setIpv4Address(fixedIp.getIpAddress())
                                                            .build();
                                                    DHCP.DHCPState dhcpState= DHCP.DHCPState.newBuilder()
                                                            .setConfiguration(dhcpConfiguration)
                                                            .build();
                                                    dhcpStateList.add(dhcpState);
                                                }
                                                String name= portStateWithEverythingFilledNB.getName()==null
                                                        ?"":portStateWithEverythingFilledNB.getName();


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
                                                // since dpm has to do everything including neighbor in 1 shot
                                                if (portStateHashSet.size() < neighborSB.size()) {
                                                    for (Port.PortConfiguration.HostInfo h : neighborSB) {
                                                        String pid = h.getMacAddress();
                                                        Port.PortConfiguration portConfiguration1 =
                                                                portConfiguration
                                                                        .toBuilder()
                                                                        .setNetworkTypeValue(Common.NetworkType.VXLAN_VALUE)
                                                                        .setId(pid)
                                                                        .setHostInfo(h)
                                                                        .setMessageTypeValue(Common.MessageType.DELTA_VALUE)
                                                                        .build();
                                                        final PortState portStateSB =
                                                                PortState.newBuilder()
                                                                        .setConfiguration(portConfiguration1)
                                                                        .setOperationType(Common.OperationType.NEIGHBOR_CREATE_UPDATE)
                                                                        .build();
                                                        portStateHashSet.add(portStateSB);
                                                    }
                                                }
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

                                                // lookup subnet entity
                                                for (InternalSubnetEntity subnetEntity1 :
                                                        portStateWithEverythingFilledNB.getSubnetEntities()) {
                                                    if(subnetEntity1.getTunnelId()==null)
                                                    {
                                                        throw new ClientOfDPMFailureException("empty tunnelId in the subnet payload!");
                                                    }
                                                    Subnet.SubnetConfiguration subnetConfiguration =
                                                            Subnet.SubnetConfiguration.newBuilder()
                                                                    .setId(subnetEntity1.getId())
                                                                    .setVpcId(subnetEntity1.getVpcId())
                                                                    .setProjectId(portStateWithEverythingFilledNB.getProjectId())
                                                                    .setCidr(subnetEntity1.getCidr())
                                                                    .setFormatVersion(FORMAT_REVISION_NUMBER)
                                                                    .setTunnelId(subnetEntity1.getTunnelId())
                                                                    .build();
                                                    Subnet.SubnetState subnetState =
                                                            Subnet.SubnetState.newBuilder()
                                                                    .setConfiguration(subnetConfiguration)
                                                                    .buildPartial();
                                                    if (networkConfiguration.getRsType().equals(Common.ResourceType.PORT))
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
                                                    if (!(networkConfiguration.getOpType().equals(Common.OperationType.CREATE)
                                                            && networkConfiguration
                                                            .getRsType()
                                                            .equals(Common.ResourceType.PORT))) {
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

                                                }
                                            });
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
                                            .addAllSubnetStates(subnetStateSet)
                                            .addSecurityGroupStates(0, securityGroupState)
                                            .addAllDhcpStates(dhcpStateList)
                                            //                          .addAllVpcStates(vpcStateSet)
                                            .build();
                            goalStateHashMap.put(eachGSOnSingleIP.getKey(), goalState);
                        });
        LOG.log(Level.INFO,
                goalStateHashMap.entrySet().toString());
        return goalStateHashMap;
    }

    /**
     * bind Host With Ports
     *
     * @param neighborInfoInSameSubenetMap same subnet neighborInfo mapping
     * @param portsInSameSubnetMap same subnet portId mapping
     * @param mapGroupedByHostIp portsList hostIp  mapping
     * @param subnetMap
     * @param vpcMap
     * @param bindingHostIP
     * @param currentPortEntity
     * @param portStates
     */
    private int bindHostWithPorts(Map<String, Set<String>> portsInSameSubnetMap
            , Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap,
                                  Map<String, List<InternalPortEntity>> mapGroupedByHostIp,
                                  Map<String, InternalSubnetEntity> subnetMap,
                                  Map<String, VpcEntity> vpcMap,
                                  int portCounter, String bindingHostIP,
                                  InternalPortEntity currentPortEntity,
                                  List<InternalPortEntity> portStates) {
        fillSubnetAndVpcToPort(subnetMap, vpcMap, currentPortEntity, portStates,
                neighborInfoInSameSubenetMap, portsInSameSubnetMap);
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
            Map<String, Set<String>> portsInSameSubnetMap) {
        Set<InternalSubnetEntity> allSubletsInOnePort = new HashSet<>();
        Set<VpcEntity> vpcEntityHashSet = new HashSet<>();

        for (com.futurewei.alcor.web.entity.port.PortEntity.FixedIp fixedIp :
                currentPortEntity.getFixedIps()) {

            if (!portsInSameSubnetMap.containsKey(fixedIp.getSubnetId())) {
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
                Set<String> tempPorts = portsInSameSubnetMap.get(fixedIp.getSubnetId());
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
     * deploy GoalState to ACA in parallel and return ACA processing result
     * to upper layer
     *
     * @param gss   bindHostIp realated goalstate
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