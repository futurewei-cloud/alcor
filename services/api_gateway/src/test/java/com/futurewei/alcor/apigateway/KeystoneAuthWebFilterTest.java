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

package com.futurewei.alcor.apigateway;

import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.apigateway.filter.KeystoneAuthGwFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}",
                "keystone.enable=true", "neutron.url_prefix=/v2.0"})
@DirtiesContext
@AutoConfigureWireMock(port = 0)
public class KeystoneAuthWebFilterTest {

    private static final String TEST_TOKEN = "gaaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_PROJECT_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String TEST_ERROR_TOKEN = "testerrortoken";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private KeystoneAuthGwFilter keystoneAuthGwFilter;

    @MockBean
    private KeystoneClient keystoneClient;

    @Before
    public void setUp(){
        ReflectionTestUtils.setField(keystoneAuthGwFilter, "keystoneClient", keystoneClient);
        when(keystoneClient.verifyToken(TEST_TOKEN)).thenReturn(TEST_PROJECT_ID);
        when(keystoneClient.verifyToken(TEST_ERROR_TOKEN)).thenReturn("");
    }

    @Test
    public void testNormal(){
        webClient
                .get().uri("/v2.0/tests")
                .header("X-Auth-Token", TEST_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .equals("test ok");

    }

    @Test
    public void testNotFound(){
        webClient
                .get().uri("/v2.0/tests/not/found")
                .header("X-Auth-Token", TEST_TOKEN)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testNoToken(){
        webClient
                .get().uri("/v2.0/tests")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void testErrorToken(){
        webClient
                .get().uri("/v2.0/tests")
                .header("X-Auth-Token", TEST_ERROR_TOKEN)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void testServerError(){
        webClient
                .post().uri("/v2.0/test/error")
                .header("X-Auth-Token", TEST_TOKEN)
                .exchange()
                .expectStatus().is5xxServerError();
    }

}
