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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CreateNetworkAclTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;

    private static List<String> subnetIds = new ArrayList<>();

    @BeforeAll
    public static void beforeAllTestCases() {
        subnetIds.add(UnitTestConfig.subnetId1);
    }

    @Test
    public void createNetworkAclWithIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.id").value(UnitTestConfig.networkAclId1));
    }

    @Test
    public void createNetworkAclWithoutIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(null,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutVpcIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                null,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        null,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutNameTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                null,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity(UnitTestConfig.networkAclId1,
                        null,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutAssociatedSubnetTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                null);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        null));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }
}
