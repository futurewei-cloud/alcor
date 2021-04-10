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

package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.futurewei.alcor.portmanager.util.ResourceBuilder.buildIpv4AddrRequest;
import static org.mockito.ArgumentMatchers.refEq;
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
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setSubnetId(UnitTestConfig.subnetId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv4Version);
        ipAddrRequest.setVpcId(UnitTestConfig.vpcId);
        ipAddrRequest.setState(null);
        ipAddrRequest.setIp(UnitTestConfig.ip1);

        Mockito.when(ipManagerRestClient.allocateIpAddress(refEq(ipAddrRequest)))
                .thenReturn(buildIpv4AddrRequest(UnitTestConfig.ip1));

        ipAddrRequest.setIp(UnitTestConfig.ip2);
        Mockito.when(ipManagerRestClient.allocateIpAddress(refEq(ipAddrRequest)))
                .thenReturn(buildIpv4AddrRequest(UnitTestConfig.ip2));

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
        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateBindingHost)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.binding:host_id").value(UnitTestConfig.nodeId2));
    }

    //@Test
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
        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.updateMacLearningEnabled)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_learning_enabled").value(UnitTestConfig.macLearningEnabled2));
    }

    //@Test
    public void updateMacAddressAndFixedIpsBulkTest() throws Exception {
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
