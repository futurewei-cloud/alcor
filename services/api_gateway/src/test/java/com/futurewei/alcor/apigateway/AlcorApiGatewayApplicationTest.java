///*
//Copyright 2019 The Alcor Authors.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//        you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software
//        distributed under the License is distributed on an "AS IS" BASIS,
//        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//        See the License for the specific language governing permissions and
//        limitations under the License.
//*/
//
//package com.futurewei.alcor.apigateway;
//
//import com.futurewei.alcor.apigateway.client.KeystoneClient;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//
//@ComponentScan(value = "com.futurewei.alcor.common.test.config")
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
//@AutoConfigureWireMock(port = 0)
//public class AlcorApiGatewayApplicationTest {
//
//    @Autowired
//    private WebTestClient webClient;
//
//    @MockBean
//    private KeystoneClient keystoneClient;
//
//    @Before
//    public void setUp(){
//    }
//
//    @Test
//    public void contextLoads() throws Exception {
//        //Stubs
//        stubFor(get(urlEqualTo("/get"))
//                .willReturn(aResponse()
//                        .withBody("{\"headers\":{\"Hello\":\"Alcor\"}}")
//                        .withHeader("Content-Type", "application/json")));
////        stubFor(get(urlEqualTo("/delay/3"))
////                .willReturn(aResponse()
////                        .withBody("no fallback")
////                        .withFixedDelay(3000)));
//
//        webClient
//                .get().uri("/get")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.headers.Hello").isEqualTo("Alcor");
//
////        webClient
////                .get().uri("/delay/3")
////                .header("Host", "www.hystrix.com")
////                .exchange()
////                .expectStatus().isOk()
////                .expectBody()
////                .consumeWith(
////                        response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
//    }
//}