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
package com.futurewei.alcor.portmanager.util;

import com.futurewei.alcor.portmanager.entity.PortBindingHost;
import com.futurewei.alcor.portmanager.entity.PortBindingRoute;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.web.entity.NodeInfo;
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

public class NetworkConfigurationUtil {

    public static InternalPortEntity buildInternalPortEntity(PortEntity portEntity,
                                                             Map<String, NodeInfo> nodeInfoMap,
                                                             Map<String, List<RouteEntity>> portRouteEntityMap,
                                                             Map<String, PortNeighbors> portNeighborsMap) throws Exception {

        String bindingHostId = portEntity.getBindingHostId();

        if (bindingHostId == null || bindingHostId.isEmpty()) {
            return null;
        }

        List<NeighborInfo> neighborInfos = null;
        if (portNeighborsMap.get(portEntity.getVpcId()).getNeighbors() != null) {
            neighborInfos = new ArrayList<>(portNeighborsMap.get(
                    portEntity.getVpcId()).getNeighbors().values());
        }

        String bindingHostIp = nodeInfoMap.get(portEntity.getId()).getLocalIp();
        List<RouteEntity> routeEntities = portRouteEntityMap.get(portEntity.getId());
        InternalPortEntity internalPortEntity = new InternalPortEntity(portEntity, routeEntities, neighborInfos, bindingHostIp);

        return internalPortEntity;
    }

    /**
     * Util method to generate a network configuration message for Data-Plane Manager.
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
                PortBindingRoute portBindingRoute = (PortBindingRoute)entity;
                if (!portRouteEntityMap.containsKey(portBindingRoute.getPortId())) {
                    portRouteEntityMap.put(portBindingRoute.getPortId(), new ArrayList<>());
                }
                portRouteEntityMap.get(portBindingRoute.getPortId()).add(portBindingRoute.getRouteEntity());
            } else if (entity instanceof PortNeighbors) {
                PortNeighbors portNeighbors = (PortNeighbors)entity;
                portNeighborsMap.put(portNeighbors.getVpcId(), portNeighbors);
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
                    // FIXME ：subnetEntity.getVpcId().hashCode() need to be changed to segmentId
                    Long tunnelId = subnetEntity.getTenantId() !=null ? Long.valueOf(getHashCode(subnetEntity.getVpcId())): null;
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
                for (Map.Entry<String, SecurityGroup> entry: securityGroupMap.entrySet()) {
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

    public static int getHashCode (String vpcId) {
        int hashcode = vpcId.hashCode();
        double num = (double)(4096 * 4096) / (double)Integer.MAX_VALUE;
        hashcode = (int)(hashcode * num);

        return hashcode;
    }
}
