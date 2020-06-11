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

import com.futurewei.alcor.portmanager.exception.*;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;

public class NetworkConfigurationUtil {

    public static InternalPortEntity buildInternalPortEntity(PortEntity portEntity, Map<String, NodeInfo> nodeInfoMap, List<NeighborInfo> neighborInfos) {

        String bindingHostId = portEntity.getBindingHostId();

        if ( bindingHostId == null || bindingHostId.isEmpty()) {
            return null;
        }

        NodeInfo nodeInfo = nodeInfoMap.get(bindingHostId);
        String bindingHostIp = nodeInfo.getLocalIp();
        InternalPortEntity internalPortEntity = new InternalPortEntity(portEntity, null, null, bindingHostIp);

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
        Map<String, SecurityGroupEntity> securityGroupEntityMap = new HashMap<>();

        for (Object entity : entities) {
            if (entity instanceof VpcEntity) {
                VpcEntity vpcEntity = (VpcEntity) entity;
                vpcEntityMap.put(vpcEntity.getId(), (VpcEntity) entity);
            } else if (entity instanceof SubnetEntity) {
                SubnetEntity subnetEntity = (SubnetEntity) entity;
                subnetEntityMap.put(subnetEntity.getId(), subnetEntity);
            } else if (entity instanceof SecurityGroupEntity) {
                SecurityGroupEntity securityGroupEntity = (SecurityGroupEntity) entity;
                securityGroupEntityMap.put(securityGroupEntity.getId(), securityGroupEntity);
            } else if (entity instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) entity;
                nodeInfoMap.put(nodeInfo.getId(), nodeInfo);
            } else if (entity instanceof PortEntity) {
                portEntities.add((PortEntity) entity);
            } else {
                throw new UnsupportedNetworkEntityException();
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
            String bindingHostId = portEntity.getBindingHostId();

            //Make sure we can get NodeInfo before building PortState
            if (bindingHostId != null && nodeInfoMap.get(bindingHostId) == null) {
                throw new NodeInfoNotFound();
            }

            //Build PortState
            // FIXME: get neighbors in the same vpc
            InternalPortEntity internalPortEntity = NetworkConfigurationUtil.buildInternalPortEntity(portEntity, nodeInfoMap, null);
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
                    InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(subnetEntity, (long) 1000);
                    networkConfigMessage.addSubnetEntity(internalSubnetEntity);
                    subnetUniqueIds.add(subnetId);
                }
            }

            //Build security group entities
            if (portEntity.getSecurityGroups() != null) {
                for (String securityGroupId : portEntity.getSecurityGroups()) {
                    SecurityGroupEntity securityGroupEntity = securityGroupEntityMap.get(securityGroupId);
                    if (securityGroupEntity == null) {
                        throw new SecurityGroupEntityNotFound();
                    }

                    if (!securityGroupUniqueIds.contains(securityGroupId)) {
                        networkConfigMessage.addSecurityGroupEntity(securityGroupEntity);
                        securityGroupUniqueIds.add(securityGroupId);
                    }
                }
            }
        }

        return networkConfigMessage;
    }
}
