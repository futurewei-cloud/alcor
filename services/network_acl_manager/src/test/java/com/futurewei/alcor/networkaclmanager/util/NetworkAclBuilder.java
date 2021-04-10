/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
