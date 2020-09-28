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

import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.web.ResourceProperties;

import java.util.ArrayList;
import java.util.List;

public class DataPlaneManagerUtil {

    public static ExclusionStrategy myExclusionStrategy =
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
    public static Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy)
            .enableComplexMapKeySerialization()
            .create();

    public static String autoGenerateUTsInput(int operationType, int resourceType, int portNum, int hostNum, boolean hasRouteEntities, boolean hasNeighbor, boolean fastPath) {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        // set operationType and resourceType
        networkConfiguration.setOpType(Common.OperationType.forNumber(operationType));
        networkConfiguration.setRsType(Common.ResourceType.forNumber(resourceType));

        // set portEntities
        List<InternalPortEntity> portEntities = new ArrayList<>();
        for (int j = 0; j < hostNum; j ++) {
            for (int i = 0; i < portNum; i ++) {
                List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
                PortEntity.FixedIp fixedIp = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f178594" + i, "192.168.2.2" + i);
                fixedIps.add(fixedIp);

                PortEntity portEntity = new PortEntity("3dda2801-d675-4688-a63f-dcda8d327f50", "f37810eb-7f83-45fa-a4d4-1b31e75399d" + i,
                        "test_cni_port" + i, "", null, true, "86:ea:77:ad:52:5" + i, "veth" + i, fastPath,
                        null, null, null, fixedIps, null, null, null,
                        "ephost_0", null, null, null, null,
                        "/var/run/netns/test_netw_ns", null, null, null, null, null,
                        null, false, null, null, 0, null, null,
                        false, false);

                List<RouteEntity> routeEntities = new ArrayList<>();
                if (hasRouteEntities) {
                    RouteEntity routeEntity = new RouteEntity("3dda2801-d675-4688-a63f-dcda8d327f50", "58f536d1-eb89-49cc-a3e6-756f70d206e" + i,
                            "default_route_rule", "", "10.0.0.0/24", "Local", 0, RouteTableType.VPC, "");
                    routeEntities.add(routeEntity);
                }

                List<NeighborInfo> neighborInfos = new ArrayList<>();
                if (hasNeighbor) {
                    NeighborInfo neighborInfo = new NeighborInfo("10.213.43.18" + j, "a87e0f87-a2d9-44ef-9194-9a62f178594" + j, "f37810eb-7f83-45fa-a4d4-1b31e75399d" + i, null, null);
                    neighborInfos.add(neighborInfo);
                }

                String bindingHostIP = "10.213.43.18" + j;

                InternalPortEntity port = new InternalPortEntity(portEntity, routeEntities, neighborInfos, bindingHostIP);
                portEntities.add(port);
            }
        }

        networkConfiguration.setPortEntities(portEntities);

        // set vpcs
        List<VpcEntity> vpcs = new ArrayList<>();
        VpcEntity vpc = new VpcEntity("3dda2801-d675-4688-a63f-dcda8d327f50", "9192a4d4-ffff-4ece-b3f0-8d36e3d88039", "test_vpc",
                "", null, false, null, null, false, null,
                null, null, false, null, false, false, false,
                null, null, null, null, null, null, null,
                null, null, null, null, null, "192.168.0.0/16");
        vpcs.add(vpc);
        networkConfiguration.setVpcs(vpcs);

        // set subnets
        List<InternalSubnetEntity> subnets = new ArrayList<>();
        SubnetEntity subnetEntity = new SubnetEntity();
        InternalSubnetEntity subnet = new InternalSubnetEntity();
        subnets.add(subnet);
        networkConfiguration.setSubnets(subnets);

        // set securityGroups
        List<SecurityGroup> securityGroups = new ArrayList<>();
        SecurityGroup securityGroup = new SecurityGroup();
        networkConfiguration.setSecurityGroups(securityGroups);


        String input = gson.toJson(networkConfiguration);
        return input;
    }

    public static String autoGenerateUTsOutput() {
        return null;
    }

}
