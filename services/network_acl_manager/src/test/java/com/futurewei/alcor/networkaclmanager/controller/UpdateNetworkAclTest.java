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
package com.futurewei.alcor.networkaclmanager.controller;

import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.networkaclmanager.repo.NetworkAclRepository;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder.buildNetworkAclWebJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdateNetworkAclTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;

    private static List<String> subnetIds = new ArrayList<>();
    private String updateNetworkAclUrl = UnitTestConfig.networkAclUrl + "/" + UnitTestConfig.networkAclId1;

    @BeforeAll
    public static void beforeAllTestCases() {
        subnetIds.add(UnitTestConfig.subnetId1);
    }
    @Test
    public void updateNetworkAclNameTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity());

        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName2,
                UnitTestConfig.vpcId1,
                subnetIds);

        this.mockMvc.perform(put(updateNetworkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.name").value(UnitTestConfig.networkAclName2));
    }

    @Test
    public void updateNetworkAclVpcIdTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity());

        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId2,
                subnetIds);

        this.mockMvc.perform(put(updateNetworkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.vpc_id").value(UnitTestConfig.vpcId2));
    }

    @Test
    public void updateNetworkAclAssociatedSubnetTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity());

        List<String> subnetList = new ArrayList<>();
        subnetList.add(UnitTestConfig.subnetId2);
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetList);

        this.mockMvc.perform(put(updateNetworkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.associated_subnets[0]").value(UnitTestConfig.subnetId2));
    }

    @Test
    public void updateNonExistNetworkAclTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(null);

        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        this.mockMvc.perform(put(updateNetworkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void updateNetworkAclNothingTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity());

        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        this.mockMvc.perform(put(updateNetworkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.id").value(UnitTestConfig.networkAclId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.name").value(UnitTestConfig.networkAclName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.vpc_id").value(UnitTestConfig.vpcId1));
    }
}
