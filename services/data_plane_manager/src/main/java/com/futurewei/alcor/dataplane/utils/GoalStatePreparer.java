/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GoalStatePreparer {
  public GoalStatePreparer() {}

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
  void convert(
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
        if (fixedIpsSet == null) fixedIpsSet = new TreeSet<String>();
        Set<String> subnetIdsSet = hostIpSubnetIdsMap.get(internalPortEntity.getBindingHostIP());
        if (subnetIdsSet == null) subnetIdsSet = new TreeSet<String>();
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
        if (fixedIps == null) fixedIps = new TreeSet<String>();
        Set<String> subnetIds = hostIpSubnetIdsMap.get(internalPortEntity.getHostIp());
        if (subnetIds == null) subnetIds = new TreeSet<String>();
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
}
