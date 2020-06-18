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
import com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GetNetworkAclRuleTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;

    private String getNetworkAclRuleUrl = UnitTestConfig.networkAclRuleUrl + "/" + UnitTestConfig.networkAclRuleId1;
    private String listNetworkAclRuleUrl = UnitTestConfig.networkAclRuleUrl;

    @Test
    public void getExistNetworkAclRuleTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAclRule(UnitTestConfig.networkAclRuleId1))
                .thenReturn(NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());

        this.mockMvc.perform(get(getNetworkAclRuleUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.id")
                        .value(UnitTestConfig.networkAclRuleId1)
                );
    }

    @Test
    public void getNonExistNetworkAclRuleTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAclRule(UnitTestConfig.networkAclRuleId1))
                .thenReturn(null);

        this.mockMvc.perform(get(getNetworkAclRuleUrl))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void listNetworkAclRuleTest() throws Exception {
        Map<String, NetworkAclRuleEntity> networkAclRuleEntityMap = new HashMap<>();
        networkAclRuleEntityMap.put(UnitTestConfig.networkAclRuleId1, NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());

        Mockito.when(networkAclRepository.getAllNetworkAclRules())
                .thenReturn(networkAclRuleEntityMap);

        this.mockMvc.perform(get(listNetworkAclRuleUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$[0].id")
                        .value(UnitTestConfig.networkAclRuleId1)
                );
    }
}
