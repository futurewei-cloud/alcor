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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class CreatePortTest extends MockRestClientAndRepository {
    @Autowired
    private MockMvc mockMvc;

    private String createPortUrl = "/project/" + UnitTestConfig.projectId + "/ports";
    private String createPortBulkUrl = createPortUrl + "/bulk";

    @Test
    public void createPortWithFixedIpsTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithFixedIps))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip1));
    }

    @Test
    public void createPortWithoutFixedIpsTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutFixedIps))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip1));
    }

    @Test
    public void createPortWithMacAddressTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithMacAddress))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_address").value(UnitTestConfig.mac1));
    }

    @Test
    public void createPortWithoutMacAddressTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutMacAddress))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_address").value(UnitTestConfig.mac1));
    }

    @Test
    public void createPortWithSecurityGroupTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithSecurityGroup))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.security_groups[0]").value(UnitTestConfig.securityGroupId1));
    }

    @Test
    public void createPortWithoutSecurityGroupTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutSecurityGroup))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1));
    }

    @Test
    public void createPortBulkTest() throws Exception {
        this.mockMvc.perform(post(createPortBulkUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.createPortBulk))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].id").value(UnitTestConfig.portId2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].fixed_ips[0].ip_address").value(UnitTestConfig.ip1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }
}
