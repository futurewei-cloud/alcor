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
