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

import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.List;

public class DataPlaneManagerValidationUtil {

    public static void validateInput(NetworkConfiguration networkConfig) throws Exception {
        if (networkConfig == null) {
            throw new NetworkConfigurationIsNull();
        }

        // rsType
        Common.ResourceType rsType = networkConfig.getRsType();
        ParametersValidator.checkResourceType(rsType);

        // opType
        Common.OperationType opType = networkConfig.getOpType();
        ParametersValidator.checkOperationType(opType);

        // ports_internal
        List<InternalPortEntity> portEntities = networkConfig.getPortEntities();
        if (portEntities == null || portEntities.size() == 0) {
            throw new PortEntitiesIsNull();
        }
        for (int i = 0; i < portEntities.size(); i ++) {
            InternalPortEntity port = portEntities.get(i);
            String portId = port.getId();
            if (isEmptyString(portId)) {
                throw new PortIdNotFound();
            }

            // binding_host_ip
            ParametersValidator.checkBindingHostIP(port);
        }

        // vpcs_internal
        List<VpcEntity> vpcs = networkConfig.getVpcs();
        if (vpcs != null) {
            for (int i = 0; i < vpcs.size(); i ++) {
                VpcEntity vpc = vpcs.get(i);

                // vpc_id
                ParametersValidator.checkVpcIdInVpcsInternal(vpc);
            }
        }

        // subnets_internal
        List<InternalSubnetEntity> subnets = networkConfig.getSubnets();
        if (subnets == null || subnets.size() == 0) {
            throw new SubnetsNotFound();
        }
        for (int i = 0; i < subnets.size(); i ++) {
            InternalSubnetEntity subnet = subnets.get(i);
            String subnetId = subnet.getId();
            if (isEmptyString(subnetId)) {
                throw new SubnetIdNotFound();
            }

            // tunnel_id
            ParametersValidator.checkTunnelId(subnet);
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
                ParametersValidator.checkHostIp(neighborInfo);

                // host_id
                ParametersValidator.checkHostId(neighborInfo);

                // port_id
                ParametersValidator.checkPortId(neighborInfo);

                // port_mac
                ParametersValidator.checkPortMac(neighborInfo);

                // port_ip
                ParametersValidator.checkPortIp(neighborInfo);

                // vpc_id
                ParametersValidator.checkVpcIdInNeighborInfo(neighborInfo);

                // subnet_id
                ParametersValidator.checkSubnetId(neighborInfo);

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

    public static boolean isEmptyString(String string) {
        return string == null || string.isEmpty();
    }

}
