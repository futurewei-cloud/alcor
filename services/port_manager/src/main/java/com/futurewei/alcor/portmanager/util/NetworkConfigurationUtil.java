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
package com.futurewei.alcor.portmanager.util;

import com.futurewei.alcor.portmanager.entity.PortBindingHost;
import com.futurewei.alcor.portmanager.entity.PortBindingRoute;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkConfigurationUtil {

    public static InternalPortEntity buildInternalPortEntity(PortEntity portEntity,
                                                             Map<String, NodeInfo> nodeInfoMap,
                                                             Map<String, List<RouteEntity>> portRouteEntityMap,
                                                             Map<String, PortNeighbors> portNeighborsMap) throws Exception {

        String bindingHostId = portEntity.getBindingHostId();

        if (bindingHostId == null || bindingHostId.isEmpty()) {
            return null;
        }

        List<NeighborInfo> neighborInfoList, filteredNeighborInfoList = null;
        if (portNeighborsMap.get(portEntity.getVpcId()).getNeighbors() != null) {
            neighborInfoList = new ArrayList<>(portNeighborsMap.get(portEntity.getVpcId()).getNeighbors().values());
            filteredNeighborInfoList = neighborInfoList.stream()
                    .filter(n -> !portEntity.getBindingHostId().equals(n.getHostId()))
                    .collect(Collectors.toList());
        }

        List<RouteEntity> routeEntities = portRouteEntityMap.get(portEntity.getId());
        String bindingHostIp = nodeInfoMap.get(portEntity.getId()).getLocalIp();

        InternalPortEntity internalPortEntity = new InternalPortEntity(portEntity, routeEntities, bindingHostIp);

        return internalPortEntity;
    }

    /**
     * Util method to generate a network configuration message for Data-Plane Manager.
     *
     * @param entities A list of network entities
     * @return NetworkConfiguration
     * @throws Exception Various exceptions that may occur during the create process
     */
    public static NetworkConfiguration buildNetworkConfiguration(List<Object> entities) throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        Map<String, NodeInfo> nodeInfoMap = new HashMap<>();
        Map<String, VpcEntity> vpcEntityMap = new HashMap<>();
        Map<String, SubnetEntity> subnetEntityMap = new HashMap<>();
        Map<String, SecurityGroup> securityGroupMap = new HashMap<>();
        Map<String, List<RouteEntity>> portRouteEntityMap = new HashMap<>();
        Map<String, PortNeighbors> portNeighborsMap = new HashMap<>();

        for (Object entity : entities) {
            if (entity instanceof VpcEntity) {
                VpcEntity vpcEntity = (VpcEntity) entity;
                vpcEntityMap.put(vpcEntity.getId(), (VpcEntity) entity);
            } else if (entity instanceof SubnetEntity) {
                SubnetEntity subnetEntity = (SubnetEntity) entity;
                subnetEntityMap.put(subnetEntity.getId(), subnetEntity);
            } else if (entity instanceof SecurityGroup) {
                SecurityGroup securityGroupEntity = (SecurityGroup) entity;
                securityGroupMap.put(securityGroupEntity.getId(), securityGroupEntity);
            } else if (entity instanceof PortBindingHost) {
                PortBindingHost portBindingHost = (PortBindingHost) entity;
                nodeInfoMap.put(portBindingHost.getPortId(), portBindingHost.getNodeInfo());
            } else if (entity instanceof PortEntity) {
                portEntities.add((PortEntity) entity);
            } else if (entity instanceof PortBindingRoute) {
                PortBindingRoute portBindingRoute = (PortBindingRoute) entity;
                if (!portRouteEntityMap.containsKey(portBindingRoute.getPortId())) {
                    portRouteEntityMap.put(portBindingRoute.getPortId(), new ArrayList<>());
                }
                portRouteEntityMap.get(portBindingRoute.getPortId()).add(portBindingRoute.getRouteEntity());
            } else if (entity instanceof PortNeighbors) {
                PortNeighbors portNeighbors = (PortNeighbors) entity;
                portNeighborsMap.put(portNeighbors.getVpcId(), portNeighbors);
            } else if (entity instanceof RouteEntity) {
                // NOTE: Router implementation is supported in the new control path in PM v2.0 implementation
                //       Please check com.futurewei.alcor.portmanager.service.PortServiceImpl for PM v2.0 implementation
            }
        }

        NetworkConfiguration networkConfigMessage = new NetworkConfiguration();

        //////////////////////////////////////////////////////////////////
        // Create a network configuration message by adding port(s) and
        //    its associated informational objects (subnet, neighbor etc.)
        // NOTE: Ensure the information objects are added once and only once
        //////////////////////////////////////////////////////////////////
        Set<String> vpcUniqueIds = new HashSet<>();
        Set<String> subnetUniqueIds = new HashSet<>();
        Set<String> securityGroupUniqueIds = new HashSet<>();

        for (PortEntity portEntity : portEntities) {
            //Build port entities
            // FIXME: get neighbors in the same vpc
            InternalPortEntity internalPortEntity = NetworkConfigurationUtil.buildInternalPortEntity(
                    portEntity, nodeInfoMap, portRouteEntityMap, portNeighborsMap);
            networkConfigMessage.addPortEntity(internalPortEntity);

            //Build vpc entities
            VpcEntity vpcEntity = vpcEntityMap.get(portEntity.getVpcId());
            if (vpcEntity == null) {
                throw new VpcEntityNotFound();
            } else if (vpcEntity.getSegmentationId() == null || vpcEntity.getSegmentationId() <= 0) {
                throw new VpcTunnelIdInvalid(vpcEntity.getId(), vpcEntity.getSegmentationId());
            }

            if (!vpcUniqueIds.contains(portEntity.getVpcId())) {
                networkConfigMessage.addVpcEntity(vpcEntity);
                vpcUniqueIds.add(portEntity.getVpcId());
            }

            //Build subnet entities
            for (PortEntity.FixedIp fixedIp : portEntity.getFixedIps()) {
                String subnetId = fixedIp.getSubnetId();
                SubnetEntity subnetEntity = subnetEntityMap.get(subnetId);
                if (subnetEntity == null) {
                    throw new SubnetEntityNotFound();
                }

                if (!subnetUniqueIds.contains(subnetId)) {
                    Long tunnelId = vpcEntity.getSegmentationId().longValue();
                    InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(subnetEntity, tunnelId);
                    networkConfigMessage.addSubnetEntity(internalSubnetEntity);
                    subnetUniqueIds.add(subnetId);
                }
            }

            //Build security group entities
            if (portEntity.getSecurityGroups() != null) {
                for (String securityGroupId : portEntity.getSecurityGroups()) {
                    SecurityGroup securityGroup = securityGroupMap.get(securityGroupId);
                    if (securityGroup == null) {
                        throw new SecurityGroupEntityNotFound();
                    }

                    if (!securityGroupUniqueIds.contains(securityGroupId)) {
                        networkConfigMessage.addSecurityGroupEntity(securityGroup);
                        securityGroupUniqueIds.add(securityGroupId);
                    }
                }
            } else {
                SecurityGroup securityGroup = null;
                for (Map.Entry<String, SecurityGroup> entry : securityGroupMap.entrySet()) {
                    if ("default".equals(entry.getValue().getName())) {
                        securityGroup = entry.getValue();
                        break;
                    }
                }

                if (securityGroup == null) {
                    throw new DefaultSecurityGroupEntityNotFound();
                }

                if (!securityGroupUniqueIds.contains(securityGroup.getId())) {
                    networkConfigMessage.addSecurityGroupEntity(securityGroup);
                    securityGroupUniqueIds.add(securityGroup.getId());
                }
            }
        }

        return networkConfigMessage;
    }

//    public static Long getTunnelId (SubnetEntity subnetEntity) {
//        if (subnetEntity.getTenantId() == null) {
//            return null;
//        }
//
//        return Long.valueOf(getHashCode(subnetEntity.getVpcId()));
//    }

    public static int getHashCode(String vpcId) {
        int hashcode = vpcId.hashCode();
        if (hashcode < 0) {
            hashcode = -hashcode;
        }
        double num = (double) (4096 * 4096) / (double) Integer.MAX_VALUE;
        hashcode = (int) (hashcode * num);

        return hashcode;
    }
}
