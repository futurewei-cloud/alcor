package com.futurewei.alcor.dataplane.entity;/*
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

import com.futurewei.alcor.dataplane.utils.DataPlaneManagerUtil;
import com.futurewei.alcor.dataplane.utils.GoalStateManager;
import com.futurewei.alcor.dataplane.utils.entity.UTIPInfo;
import com.futurewei.alcor.dataplane.utils.entity.UTL3NeighborInfoMapping;
import com.futurewei.alcor.dataplane.utils.entity.UTPortWithSubnetAndIPMapping;
import com.futurewei.alcor.dataplane.utils.entity.UTSubnetInfo;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {

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
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();

    public static void main(String[] args) throws Exception {
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping>
        Map<String, List<UTPortWithSubnetAndIPMapping>> map = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList = new ArrayList<>();

        // P1 configuration
        UTPortWithSubnetAndIPMapping mapping1 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps1 = new ArrayList<>();
        PortEntity.FixedIp fixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.20");
        fixedIps1.add(fixedIp1);

        mapping1.setBindingHostId("ephost_0");
        mapping1.setFixedIps(fixedIps1);
        mapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        mapping1.setPortMacAddress("86:ea:77:ad:52:50");
        mapping1.setPortName("test_cni_port1");
        mapping1.setVethName("veth0");

        mapList.add(mapping1);

        // P2 configuration
        UTPortWithSubnetAndIPMapping mapping2 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps2 = new ArrayList<>();
        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.21");
        fixedIps2.add(fixedIp2);

        mapping2.setBindingHostId("ephost_0");
        mapping2.setFixedIps(fixedIps2);
        mapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        mapping2.setPortMacAddress("86:ea:77:ad:52:51");
        mapping2.setPortName("test_cni_port2");
        mapping2.setVethName("veth0");

        mapList.add(mapping2);

        map.put("10.213.43.187", mapList);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets = new ArrayList<>();
        UTSubnetInfo utSubnetInfo = new UTSubnetInfo();
        utSubnetInfo.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo.setSubnetName("test_subnet0");
        utSubnetInfo.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.20", false));add(new UTIPInfo("192.168.2.21", false));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping);

        DataPlaneManagerUtil util = new DataPlaneManagerUtil();
        GoalStateManager goalStateManager = new GoalStateManager();
        NetworkConfiguration networkConfiguration = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, map, UTSubnets, L3NeighborInfoMapping, true, true, true, true, 0, true);
        String Json = gson.toJson(networkConfiguration);
        System.out.println("Input Json:" +  Json);

//        Map<String, Goalstate.GoalState> stringGoalStateMap =
//                goalStateManager.transformNorthToSouth(networkConfiguration);
//        System.out.println("DPM KeySet String:" +  stringGoalStateMap.keySet().toString());
//        System.out.println("DPM Value Size:" +  stringGoalStateMap.values().size());
//        System.out.println("DPM Port Size:" +  stringGoalStateMap
//                .get(stringGoalStateMap.keySet().iterator().next())
//                .getPortStatesList()
//                .size());

        Map<String, Goalstate.GoalState> goalStateMap = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, map, UTSubnets, L3NeighborInfoMapping, true, true, true, true, 0, true);
//        System.out.println("Auto KeySet String:" +  goalStateMap.keySet().toString());
//        System.out.println("Auto Value Size:" +  goalStateMap.values().size());
//        System.out.println("Auto Port Size:" +  goalStateMap
//                .get(goalStateMap.keySet().iterator().next())
//                .getPortStatesList()
//                .size());
        List<Goalstate.GoalState> output = new ArrayList<>();
        for (Map.Entry<String, Goalstate.GoalState> entry : goalStateMap.entrySet()) {
            Goalstate.GoalState value = (Goalstate.GoalState)entry.getValue();
            output.add(value);
        }
        String outputJson = gson.toJson(output);
        System.out.println("Output Json:" +  outputJson);

    }
}
