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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class UpdatePortTest extends MockRestClientAndRepository {
    @Autowired
    private MockMvc mockMvc;

    private String updatePortUrl = "/project/" + UnitTestConfig.projectId + "/ports" + "/" + UnitTestConfig.portId1;
    private String updatePortBulkUrl = "/project/" + UnitTestConfig.projectId + "/ports/bulk";

    @Test
    public void updateFixedIpsTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateFixedIps)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }

    @Test
    public void updateMacAddressTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateMacAddress)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_address").value(UnitTestConfig.mac2));
    }

    @Test
    public void updateSecurityGroupsTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateSecurityGroups)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.security_groups[0]").value(UnitTestConfig.securityGroupId2));
    }

    @Test
    public void updateNameTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.name").value(UnitTestConfig.portName2));
    }

    @Test
    public void updateAdminStateTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateAdminState)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.admin_state_up").value(UnitTestConfig.adminState2));
    }

    @Test
    public void updateBindingHostIdTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateBindingHost)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.binding:host_id").value(UnitTestConfig.nodeId2));
    }

    @Test
    public void updateBindingProfileTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateBindingProfile)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.binding:profile").value(UnitTestConfig.bindingProfile2));
    }

    @Test
    public void updateBindingVnicTypeTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateBindingVnicType)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.binding:vnic_type").value(UnitTestConfig.bindingVnicType2));
    }

    @Test
    public void updateDescriptionTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateDescription)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.description").value(UnitTestConfig.description2));
    }

    @Test
    public void updateDeviceIdTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateDeviceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.device_id").value(UnitTestConfig.deviceId2));
    }

    @Test
    public void updateDeviceOwnerTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateDeviceOwner)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.device_owner").value(UnitTestConfig.deviceOwner2));
    }

    @Test
    public void updateDnsDomainTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateDnsDomain)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.dns_domain").value(UnitTestConfig.dnsDomain2));
    }

    @Test
    public void updateDnsNameTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateDnsName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.dns_name").value(UnitTestConfig.dnsName2));
    }

    @Test
    public void updateQosPolicyIdTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateQosPolicyId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.qos_policy_id").value(UnitTestConfig.qosPolicyId2));
    }

    @Test
    public void updatePortSecurityEnabledTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updatePortSecurityEnabled)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.port_security_enabled").value(UnitTestConfig.portSecurityEnabled2));
    }

    @Test
    public void updateMacLearningEnabledTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateMacLearningEnabled)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_learning_enabled").value(UnitTestConfig.macLearningEnabled2));
    }

    @Test
    public void updateMacAddressAndFixedIpsBulkTest() throws Exception {
        mockRestClientsAndRepositoryOperations();

        this.mockMvc.perform(put(updatePortBulkUrl)
                .content(UnitTestConfig.updatePortBulk)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].id").value(UnitTestConfig.portId2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].mac_address").value(UnitTestConfig.mac2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].mac_address").value(UnitTestConfig.mac2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].fixed_ips[0].ip_address").value(UnitTestConfig.ip2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }
}
