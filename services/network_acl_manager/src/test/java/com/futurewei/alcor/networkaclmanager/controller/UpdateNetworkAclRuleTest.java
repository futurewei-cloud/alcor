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
package com.futurewei.alcor.networkaclmanager.controller;

import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.networkaclmanager.repo.NetworkAclRepository;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
public class UpdateNetworkAclRuleTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;
    private NetworkAclRuleEntity networkAclRuleEntity;
    private String updateNetworkAclRuleUrl = UnitTestConfig.networkAclRuleUrl + "/" + UnitTestConfig.networkAclRuleId1;

    @BeforeEach
    public void beforeEachTestCase() throws Exception {
        networkAclRuleEntity = buildNetworkAclRuleEntity1();

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1());

        Mockito.when(networkAclRepository.getNetworkAclRule(UnitTestConfig.networkAclRuleId1))
                .thenReturn(NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());

        Mockito.when(networkAclRepository.getNetworkAclRule(UnitTestConfig.networkAclRuleId2))
                .thenReturn(NetworkAclRuleBuilder.buildNetworkAclRuleEntity2());
    }

    @Test
    public void updateNetworkAclRuleNameTest() throws Exception {
        networkAclRuleEntity.setName(UnitTestConfig.networkAclRuleName2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.name")
                        .value(UnitTestConfig.networkAclRuleName2));
    }

    @Test
    public void updateNetworkAclRuleNetworkAclIdTest() throws Exception {
        NetworkAclEntity networkAclEntity = NetworkAclBuilder.buildNetworkAclEntity1();
        networkAclEntity.setId(UnitTestConfig.networkAclId2);
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId2))
                .thenReturn(networkAclEntity);

        networkAclRuleEntity.setNetworkAclId(UnitTestConfig.networkAclId2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.network_acl_id")
                        .value(UnitTestConfig.networkAclId2));
    }

    @Test
    public void updateNetworkAclRuleInvalidNetworkAclIdTest() throws Exception {
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId2))
                .thenReturn(null);

        networkAclRuleEntity.setNetworkAclId(UnitTestConfig.networkAclId2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void updateNetworkAclRuleIpPrefixTest() throws Exception {
        networkAclRuleEntity.setIpPrefix(UnitTestConfig.ipv4Prefix2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.ip_prefix")
                        .value(UnitTestConfig.ipv4Prefix2));
    }

    @Test
    public void updateNetworkAclRulePortRangeMinTest() throws Exception {
        networkAclRuleEntity.setPortRangeMin(UnitTestConfig.portRangeMin2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.port_range_min")
                        .value(UnitTestConfig.portRangeMin2));
    }

    @Test
    public void updateNetworkAclRulePortRangeMaxTest() throws Exception {
        networkAclRuleEntity.setPortRangeMax(UnitTestConfig.portRangeMax2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.port_range_max")
                        .value(UnitTestConfig.portRangeMax2));
    }

    @Test
    public void updateNetworkAclRuleIcmpTypeTest() throws Exception {
        networkAclRuleEntity.setIcmpType(UnitTestConfig.icmpType2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.icmp_type")
                        .value(UnitTestConfig.icmpType2));
    }

    @Test
    public void updateNetworkAclRuleIcmpCodeTest() throws Exception {
        networkAclRuleEntity.setIcmpCode(UnitTestConfig.icmpCode2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.icmp_code")
                        .value(UnitTestConfig.icmpCode2));
    }

    @Test
    public void updateNetworkAclRuleDirectionTest() throws Exception {
        networkAclRuleEntity.setDirection(UnitTestConfig.directionEgress);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.direction")
                        .value(UnitTestConfig.directionEgress));
    }

    @Test
    public void updateNetworkAclRuleActionTest() throws Exception {
        networkAclRuleEntity.setAction(UnitTestConfig.actionAllow);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.action")
                        .value(UnitTestConfig.actionAllow));
    }

    @Test
    public void updateNetworkAclRuleNumberNotConflictTest() throws Exception {
        networkAclRuleEntity.setNumber(UnitTestConfig.number2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.number")
                        .value(UnitTestConfig.number2));
    }

    @Test
    public void updateNetworkAclRuleNumberConflictTest() throws Exception {
        networkAclRuleEntity.setNumber(UnitTestConfig.number2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        List<NetworkAclRuleEntity> networkAclRuleEntities = new ArrayList<>();
        networkAclRuleEntities.add(NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());
        Mockito.when(networkAclRepository.getNetworkAclRulesByNumber(UnitTestConfig.number2))
                .thenReturn(networkAclRuleEntities);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void updateNetworkAclRuleEtherTypeTest() throws Exception {
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeIpv6);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.ether_type")
                        .value(UnitTestConfig.etherTypeIpv6));
    }

    @Test
    public void updateNetworkAclRuleProtocolTest() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolUdp);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.protocol")
                        .value(UnitTestConfig.protocolUdp));
    }

    @Test
    public void updateNetworkAclRuleNothingTest() throws Exception {
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);

        this.mockMvc.perform(put(updateNetworkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.network_acl_rule.id")
                        .value(UnitTestConfig.networkAclRuleId1));
    }

    @Test
    public void updateNetworkAclRuleBulkTest() throws Exception {
        String body = buildNetworkAclRuleBulkWebJsonString();

        this.mockMvc.perform(put(UnitTestConfig.networkAclRuleBulkUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
