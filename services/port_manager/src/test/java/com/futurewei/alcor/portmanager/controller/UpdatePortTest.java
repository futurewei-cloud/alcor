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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdatePortTest extends MockRestClientAndRepository {
    @Autowired
    private MockMvc mockMvc;

    private String updatePortUrl = "/project/" + UnitTestConfig.projectId + "/ports" + "/" + UnitTestConfig.portId;
    private String updatePortBulkUrl = "/project/" + UnitTestConfig.projectId + "/ports/bulk";

    @Test
    public void updateFixedIpsTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateFixedIps)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }

    @Test
    public void updateMacAddressAndFixedIpsBulkTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortBulkUrl)
                .content(UnitTestConfig.updatePortBulk)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].id").value(UnitTestConfig.portId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].id").value(UnitTestConfig.portId2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].mac_address").value(UnitTestConfig.mac2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].mac_address").value(UnitTestConfig.mac2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].fixed_ips[0].ip_address").value(UnitTestConfig.ip2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }
}
