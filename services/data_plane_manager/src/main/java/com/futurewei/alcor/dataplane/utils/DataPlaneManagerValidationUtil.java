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

import com.futurewei.alcor.dataplane.exception.validation.*;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.List;

public class DataPlaneManagerValidationUtil {

    public static void checkInputPayloadFromPortManager (NetworkConfiguration networkConfig) throws Exception {
        if (networkConfig == null) {
            throw new NetworkConfigurationIsNull();
        }

        // rsType
        Common.ResourceType rsType = networkConfig.getRsType();
        if (rsType == null) {
            throw new ResourceTypeNotValid();
        }

        // opType
        Common.OperationType opType = networkConfig.getOpType();
        if (opType == null) {
            throw new OperationTypeNotValid();
        }

        // ports_internal
        List<InternalPortEntity> portEntities = networkConfig.getPortEntities();
        if (portEntities == null || portEntities.size() == 0) {
            throw new PortEntitiesIsNull();
        }
        for (int i = 0; i < portEntities.size(); i ++) {
            InternalPortEntity port = portEntities.get(i);

            // binding_host_ip
            String bindingHostIP = port.getBindingHostIP();
            if (bindingHostIP == null || bindingHostIP.equals("")) {
                throw new BindingHostIPNotFound();
            }
        }

        // vpcs_internal
        List<VpcEntity> vpcs = networkConfig.getVpcs();
        if (vpcs != null) {
            for (int i = 0; i < vpcs.size(); i ++) {
                VpcEntity vpc = vpcs.get(i);

                // vpc_id
                String vpcId = vpc.getId();
                if (vpcId == null || vpcId.equals("")) {
                    throw new VpcIdNotFound();
                }
            }
        }

        // subnets_internal
        List<InternalSubnetEntity> subnets = networkConfig.getSubnets();
        if (subnets == null || subnets.size() == 0) {
            throw new SubnetsNotFound();
        }
        for (int i = 0; i < subnets.size(); i ++) {
            InternalSubnetEntity subnet = subnets.get(i);

            // tunnel_id
            Long tunnelId = subnet.getTunnelId();
            if (tunnelId == null) {
                throw new TunnelIdNotFound();
            }
        }

        // security_groups_internal
        List<SecurityGroup> securityGroups = networkConfig.getSecurityGroups();
        if (securityGroups != null) {
            for (int i = 0; i < securityGroups.size(); i ++) {
                SecurityGroup securityGroup = securityGroups.get(i);
            }
        }

        // neighbor_info
        List<NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos != null) {
            for (int i = 0; i < neighborInfos.size(); i ++) {
                NeighborInfo neighborInfo = neighborInfos.get(i);

                // host_ip
                String hostIp = neighborInfo.getHostIp();
                if (hostIp == null || hostIp.equals("")) {
                    throw new HostIPNotFound();
                }
            }
        }

        // neighbor_table
        List<NeighborEntry> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable != null) {
            for (int i = 0; i < neighborTable.size(); i ++) {
                NeighborEntry neighborEntry = neighborTable.get(i);
            }
        }

        // routers_internal
        List<InternalRouterInfo> internalRouterInfos = networkConfig.getInternalRouterInfos();
        if (internalRouterInfos != null) {
            for (int i = 0; i < internalRouterInfos.size(); i ++) {
                InternalRouterInfo route = internalRouterInfos.get(i);
            }
        }




    }

}
