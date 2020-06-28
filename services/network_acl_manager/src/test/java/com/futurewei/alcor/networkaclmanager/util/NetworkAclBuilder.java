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
package com.futurewei.alcor.networkaclmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclBulkWebJson;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclWebJson;

import java.util.ArrayList;
import java.util.List;

import static com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder.buildDefaultNetworkAclRules;

public class NetworkAclBuilder {
    public static NetworkAclEntity buildNetworkAclEntity1() {
        NetworkAclEntity networkAclEntity = new NetworkAclEntity();
        networkAclEntity.setId(UnitTestConfig.networkAclId1);
        networkAclEntity.setName(UnitTestConfig.networkAclName1);
        networkAclEntity.setVpcId(UnitTestConfig.vpcId1);
        networkAclEntity.setNetworkAclRuleEntities(buildDefaultNetworkAclRules());

        List<String> subnetIds = new ArrayList<>();
        subnetIds.add(UnitTestConfig.subnetId1);
        networkAclEntity.setAssociatedSubnets(subnetIds);

        return networkAclEntity;
    }

    public static NetworkAclEntity buildNetworkAclEntity2() {
        NetworkAclEntity networkAclEntity = new NetworkAclEntity();
        networkAclEntity.setId(UnitTestConfig.networkAclId2);
        networkAclEntity.setName(UnitTestConfig.networkAclName2);
        networkAclEntity.setVpcId(UnitTestConfig.vpcId2);

        List<String> subnetIds = new ArrayList<>();
        subnetIds.add(UnitTestConfig.subnetId2);
        networkAclEntity.setAssociatedSubnets(subnetIds);

        return networkAclEntity;
    }

    public static List<NetworkAclEntity> buildNetworkAclEntities() {
        NetworkAclEntity networkAclEntity1 = buildNetworkAclEntity1();
        NetworkAclEntity networkAclEntity2 = buildNetworkAclEntity2();

        List<NetworkAclEntity> networkAclEntities = new ArrayList<>();
        networkAclEntities.add(networkAclEntity1);
        networkAclEntities.add(networkAclEntity2);

        return networkAclEntities;
    }

    public static NetworkAclEntity buildNetworkAclEntity1(String id, String name, String vpcId,
                                                          List<String> subnetIds) {
        NetworkAclEntity networkAclEntity = new NetworkAclEntity();
        networkAclEntity.setId(id);
        networkAclEntity.setName(name);
        networkAclEntity.setVpcId(vpcId);
        networkAclEntity.setAssociatedSubnets(subnetIds);
        return networkAclEntity;
    }

    public static NetworkAclWebJson buildNetworkAclWebJson() {
        return new NetworkAclWebJson(buildNetworkAclEntity1());
    }

    public static NetworkAclWebJson buildNetworkAclWebJson(String id, String name,
                                                           String vpcId, List<String> subnetIds) {
        return new NetworkAclWebJson(buildNetworkAclEntity1(id, name, vpcId, subnetIds));
    }

    public static String buildNetworkAclWebJsonString() throws Exception {
        NetworkAclWebJson networkAclWebJson = new NetworkAclWebJson(buildNetworkAclEntity1());

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclWebJson);
    }

    public static String buildNetworkAclBulkWebJsonString() throws Exception {
        List<NetworkAclEntity> networkAclEntities = buildNetworkAclEntities();
        NetworkAclBulkWebJson networkAclBulkWebJson = new NetworkAclBulkWebJson(networkAclEntities);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclBulkWebJson);
    }

    public static String buildNetworkAclWebJsonString(String id, String name, String vpcId,
                                                      List<String> subnetIds) throws Exception {
        NetworkAclWebJson networkAclWebJson = new NetworkAclWebJson(buildNetworkAclEntity1(id, name, vpcId, subnetIds));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(networkAclWebJson);
    }
}
