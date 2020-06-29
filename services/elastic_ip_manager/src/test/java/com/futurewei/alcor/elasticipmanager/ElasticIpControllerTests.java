/*
Copyright 2020 The Alcor Authors.

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
package com.futurewei.alcor.elasticipmanager;

import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.elasticipmanager.config.UnitTestConfig;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.web.entity.elasticip.ElasticIp;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class ElasticIpControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticIpRangeService elasticIpRangeService;

    @MockBean
    private ElasticIpService elasticIpService;

    private String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
    private String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + UnitTestConfig.elasticIp1;
    private String getEipsUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
    private String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + UnitTestConfig.elasticIp1;
    private String putEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + UnitTestConfig.elasticIp1;

    private String createRangeUri = "/project/" + UnitTestConfig.projectId1 + "/elasticipranges";
    private String getRangeUri = "/project/" + UnitTestConfig.projectId1 + "/elasticipranges/" +
            UnitTestConfig.elasticIpRange1;
    private String getRangesUri = "/project/" + UnitTestConfig.projectId1 + "/elasticipranges";
    private String updateRangeUri = "/project/" + UnitTestConfig.projectId1 + "/elasticipranges/" +
            UnitTestConfig.elasticIpRange1;
    private String putRangeUri = "/project/" + UnitTestConfig.projectId1 + "/elasticipranges/" +
            UnitTestConfig.elasticIpRange1;

    @Test
    public void createElasticIp_create_pass () throws Exception {
        ElasticIp eip = new ElasticIp(
                UnitTestConfig.projectId1,
                UnitTestConfig.elasticIp1,
                UnitTestConfig.elasticIpName1,
                UnitTestConfig.elasticIpDescription1,
                UnitTestConfig.elasticIpRange1,
                UnitTestConfig.elasticIpVersion1,
                UnitTestConfig.elasticIpv4Address1,
                UnitTestConfig.elasticIpPort1,
                UnitTestConfig.elasticIpPrivateIpVersion1,
                UnitTestConfig.elasticIpPrivateIp1,
                UnitTestConfig.elasticIpDnsName1,
                UnitTestConfig .elasticIpDnsDomain1
        );
        ElasticIpInfo eipInfo = new ElasticIpInfo(eip);

        Mockito.when(elasticIpService.createElasticIp(eipInfo))
                .thenReturn(eipInfo);

        this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.ElasticIpInfoWithPort))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.id").value(UnitTestConfig.elasticIp1));
    }

    @Before
    public void init() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }
}
