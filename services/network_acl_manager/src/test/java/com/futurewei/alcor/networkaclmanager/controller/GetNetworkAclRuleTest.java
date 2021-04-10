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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
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
