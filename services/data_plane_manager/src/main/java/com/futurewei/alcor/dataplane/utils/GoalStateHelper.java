package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Neighbor;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoalStateHelper {
  public GoalStateHelper() {}

  void createNeighborHelper(
      NetworkConfiguration networkConfiguration,
      Map<String, String> ipPortIdMap,
      Map<String, String> ipMacMap,
      Map<String, String> ipSubnetIdMap,
      Map<String, String> ipHostIpMap,
      Map<String, Set<String>> hostIpFixedIpsMap,
      Map<String, Set<String>> hostIpSubnetIdsMap,
      Map<String, InternalSubnetEntity> subnetIdSubnetsMap,
      Map<String, InternalPortEntity> portIdPortMap,
      Map<String, NeighborInfo> portIdNeighborInfoMap,
      String currentGroupHostIp,
      Map<String, Neighbor.NeighborState> neighborStates,
      Set<String> brandNewIps,
      String ip,
      Neighbor.NeighborType l2) {
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
  }

  void add2SubnetStates(
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
            .setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER)
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
  void createNeighborState(
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
            .setFormatVersion(GoalStateManager.FORMAT_REVISION_NUMBER)
            .setRevisionNumber(Port.PortConfiguration.REVISION_NUMBER_FIELD_NUMBER)
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
  Common.OperationType getOperationType(
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
  int bindHostWithPorts(
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
  void fillSubnetAndVpcToPort(
      Map<String, InternalSubnetEntity> subnetMap,
      Map<String, VpcEntity> vpcMap,
      InternalPortEntity currentPortEntity,
      List<InternalPortEntity> portStates,
      Map<String, Set<NeighborInfo>> neighborInfoInSameSubenetMap,
      Map<String, Set<String>> portsInSameSubnetMap,
      int type) {

    Set<InternalSubnetEntity> allSubletsInOnePort = new HashSet<InternalSubnetEntity>();
    Set<VpcEntity> vpcEntityHashSet = new HashSet<VpcEntity>();

    for (PortEntity.FixedIp fixedIp : currentPortEntity.getFixedIps()) {
      String checkString = null;
      if (type == 2) {
        checkString = fixedIp.getSubnetId();
      } else if (type == 3) {
        checkString = subnetMap.get(fixedIp.getSubnetId()).getVpcId();
      }
      if (!portsInSameSubnetMap.containsKey(checkString)) {
        Set<String> tempPorts = new HashSet<String>();
        Set<NeighborInfo> tempNeighbor = new HashSet<NeighborInfo>();
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
  void groupNeighborAndPortsBySubnet(
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
}
