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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.PortState;
import com.futurewei.alcor.web.entity.PortStateJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class PortControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String createPortUrl = "/project/" + UnitTestConfig.projectId + "/ports";
    private String updatePortUrl = createPortUrl + "/" + UnitTestConfig.portId;
    private String deletePortUrl = updatePortUrl;
    private String getPortUrl = updatePortUrl;
    private String listPortUrl = createPortUrl;

    @Test
    public void createPortTest() throws Exception {
        PortState portStat = new PortState();
        portStat.setId(UnitTestConfig.portId);
        portStat.setVpcId(UnitTestConfig.vpcId);
        portStat.setTenantId(UnitTestConfig.tenantId);
        portStat.setTenantId(UnitTestConfig.tenantId);

        PortState.FixedIp fixedIp = new PortState.FixedIp();
        fixedIp.setSubnetId(UnitTestConfig.subnetId);
        fixedIp.setIpAddress(UnitTestConfig.ipAddress1);
        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(fixedIp);

        portStat.setFixedIps(fixedIps);

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroup);
        portStat.setSecurityGroups(securityGroups);

        PortStateJson portStateJson = new PortStateJson();
        portStateJson.setPortState(portStat);

        ObjectMapper objectMapper = new ObjectMapper();
        String portStateStr = objectMapper.writeValueAsString(portStateJson);

        this.mockMvc.perform(post(createPortUrl)
                .content(portStateStr)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void updatePortTest() throws Exception {
        PortState portStat = new PortState();
        portStat.setId(UnitTestConfig.portId);
        portStat.setVpcId(UnitTestConfig.vpcId);
        portStat.setTenantId(UnitTestConfig.tenantId);
        portStat.setTenantId(UnitTestConfig.tenantId);

        PortState.FixedIp fixedIp = new PortState.FixedIp();
        fixedIp.setSubnetId(UnitTestConfig.subnetId);
        fixedIp.setIpAddress(UnitTestConfig.ipAddress2);
        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(fixedIp);

        portStat.setFixedIps(fixedIps);

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroup);
        portStat.setSecurityGroups(securityGroups);

        PortStateJson portStateJson = new PortStateJson();
        portStateJson.setPortState(portStat);

        ObjectMapper objectMapper = new ObjectMapper();
        String portStateStr = objectMapper.writeValueAsString(portStateJson);

        this.mockMvc.perform(put(updatePortUrl)
                .content(portStateStr)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getPortTest() throws Exception {
        this.mockMvc.perform(get(getPortUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void listPortTest() throws Exception {
        this.mockMvc.perform(get(listPortUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deletePortTest() throws Exception {
        this.mockMvc.perform(delete(deletePortUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
