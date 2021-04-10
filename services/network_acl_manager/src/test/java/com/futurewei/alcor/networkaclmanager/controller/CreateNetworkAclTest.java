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
import com.futurewei.alcor.networkaclmanager.util.SubnetBuilder;
import com.futurewei.alcor.networkaclmanager.util.VpcBuilder;
import com.futurewei.alcor.web.restclient.SubnetManagerRestClient;
import com.futurewei.alcor.web.restclient.VpcManagerRestClient;
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

import static com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder.buildNetworkAclBulkWebJsonString;
import static com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder.buildNetworkAclWebJsonString;
import static com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder.buildDefaultNetworkAclRules;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
public class CreateNetworkAclTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NetworkAclRepository networkAclRepository;

    @MockBean
    private VpcManagerRestClient vpcManagerRestClient;

    @MockBean
    private SubnetManagerRestClient subnetManagerRestClient;

    private static List<String> subnetIds = new ArrayList<>();

    @BeforeEach
    public void beforeEachTestCase() throws Exception {
        subnetIds.add(UnitTestConfig.subnetId1);

        Mockito.when(vpcManagerRestClient.getVpc(UnitTestConfig.projectId, UnitTestConfig.vpcId1))
                .thenReturn(VpcBuilder.buildVpcWebJson());

        Mockito.when(vpcManagerRestClient.getVpc(UnitTestConfig.projectId, UnitTestConfig.vpcId2))
                .thenReturn(VpcBuilder.buildVpcWebJson());

        Mockito.when(subnetManagerRestClient.getSubnet(UnitTestConfig.projectId, UnitTestConfig.subnetId1))
                .thenReturn(SubnetBuilder.buildSubnetStateJson());

        Mockito.when(subnetManagerRestClient.getSubnet(UnitTestConfig.projectId, UnitTestConfig.subnetId2))
                .thenReturn(SubnetBuilder.buildSubnetStateJson());

        Mockito.when(networkAclRepository.getDefaultNetworkAclRules())
                .thenReturn(buildDefaultNetworkAclRules());
    }

    @Test
    public void createNetworkAclWithIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_acl.id").value(UnitTestConfig.networkAclId1));
    }

    @Test
    public void createNetworkAclWithoutIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(null,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutVpcIdTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                null,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        null,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutNameTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                null,
                UnitTestConfig.vpcId1,
                subnetIds);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1(UnitTestConfig.networkAclId1,
                        null,
                        UnitTestConfig.vpcId1,
                        subnetIds));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createNetworkAclWithoutAssociatedSubnetTest() throws Exception {
        String body = buildNetworkAclWebJsonString(UnitTestConfig.networkAclId1,
                UnitTestConfig.networkAclName1,
                UnitTestConfig.vpcId1,
                null);

        Mockito.when(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1(UnitTestConfig.networkAclId1,
                        UnitTestConfig.networkAclName1,
                        UnitTestConfig.vpcId1,
                        null));

        this.mockMvc.perform(post(UnitTestConfig.networkAclUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void createNetworkAclBulkTest() throws Exception {
        String body = buildNetworkAclBulkWebJsonString();
        this.mockMvc.perform(post(UnitTestConfig.networkAclBulkUrl)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }
}
