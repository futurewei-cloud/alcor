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

import static com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
public class CreateNetworkAclRuleTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;

    private NetworkAclRuleEntity networkAclRuleEntity;

    @BeforeEach
    public void beforeEachTestCase() throws Exception {
        networkAclRuleEntity = buildNetworkAclRuleEntity1();
        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1());
    }

    @Test
    public void createNetworkAclRuleWithIdTest() throws Exception {
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl_rule.id").value(UnitTestConfig.networkAclRuleId1));
    }

    @Test
    public void createNetworkAclRuleWithoutIdTest() throws Exception {
        networkAclRuleEntity.setId(null);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleWithoutNetworkAclIdTest() throws Exception {
        networkAclRuleEntity.setNetworkAclId(null);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleWithoutNumberTest() throws Exception {
        networkAclRuleEntity.setNumber(null);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleWithoutIpPrefixTest() throws Exception {
        networkAclRuleEntity.setIpPrefix(null);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleWithoutDirectionTest() throws Exception {
        networkAclRuleEntity.setDirection(null);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleDirectionEgressTest() throws Exception {
        networkAclRuleEntity.setDirection(UnitTestConfig.directionEgress);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleInvalidDirectionTest() throws Exception {
        networkAclRuleEntity.setDirection(UnitTestConfig.directionInvalid);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleActionAllowTest() throws Exception {
        networkAclRuleEntity.setAction(UnitTestConfig.actionAllow);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleInvalidActionTest() throws Exception {
        networkAclRuleEntity.setAction(UnitTestConfig.actionInvalid);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidNumber1Test() throws Exception {
        networkAclRuleEntity.setNumber(UnitTestConfig.numberInvalid1);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidNumber2Test() throws Exception {
        networkAclRuleEntity.setNumber(UnitTestConfig.numberInvalid2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleEtherTypeIpv6Test() throws Exception {
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeIpv6);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleInvalidEtherTypeTest() throws Exception {
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeInvalid);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleProtocolUdpTest() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolUdp);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleProtocolIcmpTest() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmp);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleProtocolIcmpv6Test() throws Exception {
        networkAclRuleEntity.setEtherType(UnitTestConfig.etherTypeIpv6);
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmpv6);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void createNetworkAclRuleInvalidProtocolTest() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolInvalid);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidPortRangeMin1Test() throws Exception {
        networkAclRuleEntity.setPortRangeMin(UnitTestConfig.portRangeMinInvalid1);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidPortRangeMin2Test() throws Exception {
        networkAclRuleEntity.setPortRangeMin(UnitTestConfig.portRangeMinInvalid2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidPortRangeMax1Test() throws Exception {
        networkAclRuleEntity.setPortRangeMax(UnitTestConfig.portRangeMaxInvalid1);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidPortRangeMax2Test() throws Exception {
        networkAclRuleEntity.setPortRangeMax(UnitTestConfig.portRangeMaxInvalid2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidIcmpType1Test() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmp);
        networkAclRuleEntity.setIcmpType(UnitTestConfig.icmpTypeInvalid1);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidIcmpType2Test() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmp);
        networkAclRuleEntity.setIcmpType(UnitTestConfig.icmpTypeInvalid2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidIcmpCode1Test() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmp);
        networkAclRuleEntity.setIcmpCode(UnitTestConfig.icmpCodeInvalid1);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleInvalidIcmpCode2Test() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmp);
        networkAclRuleEntity.setIcmpCode(UnitTestConfig.icmpCodeInvalid2);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleEtherTypeIpPrefixConflictTest() throws Exception {
        networkAclRuleEntity.setIpPrefix(UnitTestConfig.ipv6Prefix);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleEtherTypeProtocolConflictTest() throws Exception {
        networkAclRuleEntity.setProtocol(UnitTestConfig.protocolIcmpv6);
        String body = buildNetworkAclRuleWebJsonString(networkAclRuleEntity);
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void createNetworkAclRuleBulkTest() throws Exception {
        String body = buildNetworkAclRuleBulkWebJsonString();
        this.mockMvc.perform(post(UnitTestConfig.networkAclRuleBulkUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }
}
