/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.apigateway;

import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.apigateway.filter.KeystoneAuthGwFilter;
import com.futurewei.alcor.common.entity.TokenEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@RunWith(MockitoJUnitRunner.class)
public class KeystoneAuthGwFilterTest {

    private static final String AUTHORIZE_TOKEN = "X-Auth-Token";
    private static final String TEST_TOKEN = "gaaaaaBex0xWssdfsadfDSSDFSDF";
    private static final String TEST_PROJECT_ID = "aaaaaaaabbbbccccddddeeeeeeeeeeee";
    private static final String TEST_ERROR_TOKEN = "testerrortoken";

    private KeystoneAuthGwFilter filter;

    @Mock
    private KeystoneClient keystoneClient;

    @Before
    public void setUp(){
        filter = new KeystoneAuthGwFilter();
        ReflectionTestUtils.setField(filter, "keystoneClient", keystoneClient);
        ReflectionTestUtils.setField(filter, "neutronUrlPrefix", "/v2.0");
        TokenEntity tokenEntity = new TokenEntity(TEST_TOKEN, false);
        tokenEntity.setProjectId(TEST_PROJECT_ID);
        when(keystoneClient.verifyToken(TEST_TOKEN)).thenReturn(Optional.of(tokenEntity));
        when(keystoneClient.verifyToken(TEST_ERROR_TOKEN)).thenReturn(Optional.empty());
    }

    @Test
    public void testNormal(){
        URI url = URI.create("http://localhost:8080/v2.0/tests?a=b&c=d[]");
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, url)
                .header(AUTHORIZE_TOKEN, TEST_TOKEN).build();
        ServerWebExchange exchange = testFilter(request, "http://myhost");

        URI uri = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

        assertThat(uri).hasScheme("http")
                .hasPath("/project/" +  TEST_PROJECT_ID + "/tests")
                .hasParameter("a", "b")
                .hasParameter("c", "d[]");

    }

    @Test
    public void testErrorToken(){
        URI url = URI.create("http://localhost:8080/v2.0/tests?a=b&c=d[]");
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, url)
                .header(AUTHORIZE_TOKEN, TEST_ERROR_TOKEN).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        testFilter(exchange, "http://myhost");

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testNoToken(){
        URI url = URI.create("http://localhost:8080/v2.0/tests?a=b&c=d[]");
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, url).build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        testFilter(exchange, "http://myhost");

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ServerWebExchange testFilter(MockServerHttpRequest request, String url){
        Route route = Route.async().id("test").order(0).predicate(swe -> true)
                .uri(url).build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, route);

        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);
        verify(filterChain).filter(captor.capture());
        return captor.getValue();
    }

    private void testFilter(ServerWebExchange exchange, String url){
        Route route = Route.async().id("test").order(0).predicate(swe -> true)
                .uri(url).build();

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, route);

        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        filter.filter(exchange, filterChain);
    }
}
