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

package com.futurewei.alcor.apigateway;

import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.apigateway.filter.KeystoneAuthGwFilter;
import com.futurewei.alcor.common.entity.TokenEntity;
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

import java.util.Optional;

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
    private static final String TEST_PROJECT_ID = "aaaaaaaabbbbccccddddeeeeeeeeeeee";
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
        TokenEntity tokenEntity = new TokenEntity(TEST_TOKEN, false);
        tokenEntity.setProjectId(TEST_PROJECT_ID);
        when(keystoneClient.verifyToken(TEST_TOKEN)).thenReturn(Optional.of(tokenEntity));
        when(keystoneClient.verifyToken(TEST_ERROR_TOKEN)).thenReturn(Optional.empty());
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
