/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.securitygroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.securitygroup.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
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
    public void Test03_createSecurityGroupBulkTest() throws Exception {
        SecurityGroup securityGroup1 = new SecurityGroup();
        securityGroup1.setId(UnitTestConfig.securityGroupId);
        securityGroup1.setName(UnitTestConfig.securityGroupName);
        securityGroup1.setProjectId(UnitTestConfig.projectId);
        securityGroup1.setTenantId(UnitTestConfig.tenantId);
        securityGroup1.setDescription(UnitTestConfig.securityGroupDescription);

        SecurityGroup securityGroup2 = new SecurityGroup();
        securityGroup2.setId(UnitTestConfig.securityGroupId2);
        securityGroup2.setName(UnitTestConfig.securityGroupName2);
        securityGroup2.setProjectId(UnitTestConfig.projectId);
        securityGroup2.setTenantId(UnitTestConfig.tenantId);
        securityGroup2.setDescription(UnitTestConfig.securityGroupDescription2);

        List<SecurityGroup> securityGroups = new ArrayList<>();
        securityGroups.add(securityGroup1);
        securityGroups.add(securityGroup2);
        SecurityGroupBulkJson securityGroupBulkJson = new SecurityGroupBulkJson(securityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupBulkJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupBulkUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test04_updateSecurityGroupTest() throws Exception {
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
    public void Test05_getSecurityGroupTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupUrl + "/" + UnitTestConfig.securityGroupId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test06_getDefaultSecurityGroupTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupUrl + "/default/" + UnitTestConfig.tenantId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test07_listSecurityGroupTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test08_createSecurityGroupRuleTest() throws Exception {
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
    public void Test09_createSecurityGroupRuleTest() throws Exception {
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
    public void Test10_createSecurityGroupRuleTest() throws Exception {
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
    public void Test11_createSecurityGroupRuleBulkTest() throws Exception {
        SecurityGroupRule securityGroupRule1 = new SecurityGroupRule();
        securityGroupRule1.setProjectId(UnitTestConfig.projectId);
        securityGroupRule1.setTenantId(UnitTestConfig.tenantId);
        securityGroupRule1.setSecurityGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule1.setDirection(UnitTestConfig.direction);
        securityGroupRule1.setProtocol(UnitTestConfig.protocolTcp);
        securityGroupRule1.setPortRangeMin(UnitTestConfig.portRangeMin);
        securityGroupRule1.setPortRangeMax(UnitTestConfig.portRangeMax);
        securityGroupRule1.setEtherType(UnitTestConfig.etherType);

        SecurityGroupRule securityGroupRule2 = new SecurityGroupRule();
        securityGroupRule2.setProjectId(UnitTestConfig.projectId);
        securityGroupRule2.setTenantId(UnitTestConfig.tenantId);
        securityGroupRule2.setSecurityGroupId(UnitTestConfig.securityGroupId);
        securityGroupRule2.setDirection(UnitTestConfig.direction2);
        securityGroupRule2.setProtocol(UnitTestConfig.protocolUdp);
        securityGroupRule2.setPortRangeMin(UnitTestConfig.portRangeMin);
        securityGroupRule2.setPortRangeMax(UnitTestConfig.portRangeMax);
        securityGroupRule2.setEtherType(UnitTestConfig.etherType);

        List<SecurityGroupRule> securityGroupRules = new ArrayList<>();
        securityGroupRules.add(securityGroupRule1);
        securityGroupRules.add(securityGroupRule2);
        SecurityGroupRuleBulkJson securityGroupRuleBulkJson = new SecurityGroupRuleBulkJson(securityGroupRules);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(securityGroupRuleBulkJson);

        this.mockMvc.perform(post(UnitTestConfig.securityGroupRuleBulkUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test12_getSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupRuleUrl + "/" + UnitTestConfig.securityGroupRuleId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test13_listSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.securityGroupRuleUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test14_bindSecurityGroupTest() throws Exception {
        PortBindingSecurityGroup portBindingSecurityGroup = new PortBindingSecurityGroup();
        portBindingSecurityGroup.setPortId(UnitTestConfig.portId);
        portBindingSecurityGroup.setSecurityGroupId(UnitTestConfig.securityGroupId);
        List<PortBindingSecurityGroup>  portBindingSecurityGroups = new ArrayList<>();
        portBindingSecurityGroups.add(portBindingSecurityGroup);

        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(portBindingSecurityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(portBindingSecurityGroupsJson);

        this.mockMvc.perform(post(UnitTestConfig.bindSecurityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test15_unbindSecurityGroupTest() throws Exception {
        PortBindingSecurityGroup portBindingSecurityGroup = new PortBindingSecurityGroup();
        portBindingSecurityGroup.setPortId(UnitTestConfig.portId);
        portBindingSecurityGroup.setSecurityGroupId(UnitTestConfig.securityGroupId);
        List<PortBindingSecurityGroup>  portBindingSecurityGroups = new ArrayList<>();
        portBindingSecurityGroups.add(portBindingSecurityGroup);

        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(portBindingSecurityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(portBindingSecurityGroupsJson);

        this.mockMvc.perform(post(UnitTestConfig.unbindSecurityGroupUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test16_deleteSecurityGroupRuleTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.securityGroupRuleUrl + "/" +
                UnitTestConfig.securityGroupRuleId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test17_deleteSecurityGroupTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.securityGroupUrl + "/" +
                UnitTestConfig.securityGroupId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test18_concurrentBindSecurityGroupTest() throws Throwable {
        PortBindingSecurityGroup portBindingSecurityGroup = new PortBindingSecurityGroup();
        portBindingSecurityGroup.setPortId(UnitTestConfig.portId);
        portBindingSecurityGroup.setSecurityGroupId(UnitTestConfig.securityGroupId);
        List<PortBindingSecurityGroup>  portBindingSecurityGroups = new ArrayList<>();
        portBindingSecurityGroups.add(portBindingSecurityGroup);

        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(portBindingSecurityGroups);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(portBindingSecurityGroupsJson);

        Test02_createSecurityGroupTest();

        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threads[i] = new Thread(()-> {
                try {
                    this.mockMvc.perform(post(UnitTestConfig.bindSecurityGroupUrl)
                            .content(body)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated())
                            .andDo(print());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < 4; i++) {
            threads[i].start();
        }

        for (int i = 0; i < 4; i++) {
            threads[i].join();
        }
    }
}
