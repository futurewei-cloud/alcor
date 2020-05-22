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
package com.futurewei.alcor.securitygroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.securitygroup.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityGroupTest extends MockIgniteServer {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void Test01_createDefaultSecurityGroupTest() throws Exception {
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setName(UnitTestConfig.defaultSecurityGroupName);
        securityGroup.setProjectId(UnitTestConfig.projectId);
        securityGroup.setTenantId(UnitTestConfig.tenantId);

        SecurityGroupJson securityGroupJson = new SecurityGroupJson(securityGroup);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test02_createSecurityGroupTest() throws Exception {
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setId(UnitTestConfig.securityGroupId);
        securityGroup.setName(UnitTestConfig.securityGroupName);
        securityGroup.setProjectId(UnitTestConfig.projectId);
        securityGroup.setTenantId(UnitTestConfig.tenantId);
        securityGroup.setDescription(UnitTestConfig.securityGroupDescription);

        SecurityGroupJson securityGroupJson = new SecurityGroupJson(securityGroup);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test03_updateSecurityGroupTest() throws Exception {
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setId(UnitTestConfig.securityGroupId);
        securityGroup.setName(UnitTestConfig.securityGroupName2);
        securityGroup.setDescription(UnitTestConfig.securityGroupDescription2);

        SecurityGroupJson securityGroupJson = new SecurityGroupJson(securityGroup);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupJson);

        this.mockMvc.perform(put(UnitTestConfig.securityGroupUrl + "/" + UnitTestConfig.securityGroupId)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test04_getSecurityGroupTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupUrl + "/" + UnitTestConfig.securityGroupId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test05_listSecurityGroupTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test06_createSecurityGroupRuleTest() throws Exception {
        SecurityGroupRule securityGroupRule = new SecurityGroupRule();
        securityGroupRule.setId(UnitTestConfig.securityGroupRuleId);
        securityGroupRule.setProjectId(UnitTestConfig.projectId);
        securityGroupRule.setTenantId(UnitTestConfig.tenantId);

        securityGroupRule.setSecurityGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule.setDirection(UnitTestConfig.direction);
        securityGroupRule.setProtocol(UnitTestConfig.protocolTcp);
        securityGroupRule.setPortRangeMin(UnitTestConfig.portRangeMin);
        securityGroupRule.setPortRangeMax(UnitTestConfig.portRangeMax);
        securityGroupRule.setEtherType(UnitTestConfig.etherType);

        SecurityGroupRuleJson securityGroupRuleJson = new SecurityGroupRuleJson(securityGroupRule);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupRuleJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test07_createSecurityGroupRuleTest() throws Exception {
        SecurityGroupRule securityGroupRule = new SecurityGroupRule();
        securityGroupRule.setProjectId(UnitTestConfig.projectId);
        securityGroupRule.setTenantId(UnitTestConfig.tenantId);

        securityGroupRule.setSecurityGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule.setDirection(UnitTestConfig.direction);
        securityGroupRule.setRemoteGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule.setProtocol(UnitTestConfig.protocolTcp);
        securityGroupRule.setPortRangeMin(UnitTestConfig.portRangeMin);
        securityGroupRule.setPortRangeMax(UnitTestConfig.portRangeMax);
        securityGroupRule.setEtherType(UnitTestConfig.etherType);

        SecurityGroupRuleJson securityGroupRuleJson = new SecurityGroupRuleJson(securityGroupRule);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupRuleJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test08_createSecurityGroupRuleTest() throws Exception {
        SecurityGroupRule securityGroupRule = new SecurityGroupRule();
        securityGroupRule.setProjectId(UnitTestConfig.projectId);
        securityGroupRule.setTenantId(UnitTestConfig.tenantId);

        securityGroupRule.setSecurityGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule.setDirection(UnitTestConfig.direction);
        securityGroupRule.setRemoteIpPrefix(UnitTestConfig.remoteIpPrefix);
        securityGroupRule.setProtocol(UnitTestConfig.protocolIcmp);
        securityGroupRule.setPortRangeMin(UnitTestConfig.portRangeMinIcmp);
        securityGroupRule.setPortRangeMax(UnitTestConfig.portRangeMaxIcmp);
        securityGroupRule.setEtherType(UnitTestConfig.etherType);

        SecurityGroupRuleJson securityGroupRuleJson = new SecurityGroupRuleJson(securityGroupRule);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupRuleJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test09_getSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupRuleUrl + "/" + UnitTestConfig.securityGroupRuleId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test10_listSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupRuleUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test11_bindSecurityGroupTest() throws Exception {
        PortSecurityGroupsJson portSecurityGroupsJson = new PortSecurityGroupsJson();
        portSecurityGroupsJson.setPortId(UnitTestConfig.portId);
        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroupId);
        portSecurityGroupsJson.setSecurityGroups(securityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(portSecurityGroupsJson);

        this.mockMvc.perform(post(UnitTestConfig.bindSecurityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test12_unbindSecurityGroupTest() throws Exception {
        PortSecurityGroupsJson portSecurityGroupsJson = new PortSecurityGroupsJson();
        portSecurityGroupsJson.setPortId(UnitTestConfig.portId);
        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroupId);
        portSecurityGroupsJson.setSecurityGroups(securityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(portSecurityGroupsJson);

        this.mockMvc.perform(post(UnitTestConfig.unbindSecurityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test13_deleteSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.securityGroupRuleUrl + "/" +
                UnitTestConfig.securityGroupRuleId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test14_deleteSecurityGroupTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.securityGroupUrl + "/" +
                UnitTestConfig.securityGroupId))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
