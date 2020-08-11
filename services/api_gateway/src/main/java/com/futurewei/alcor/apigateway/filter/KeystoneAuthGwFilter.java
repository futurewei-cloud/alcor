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

package com.futurewei.alcor.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.common.entity.TokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.futurewei.alcor.common.utils.ControllerUtil.TOKEN_INFO_HEADER;

public class KeystoneAuthGwFilter implements GlobalFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneAuthGwFilter.class);

    private static final String AUTHORIZE_TOKEN = "X-Auth-Token";
    private static final String VPC_NAME = "vpcs";
    private static final String VPC_REPLACE_NAME = "networks";
    private static final int KEYSTONE_FILTER_ORDERED = -100;

    @Autowired
    private KeystoneClient keystoneClient;

    @Value("${neutron.url_prefix}")
    private String neutronUrlPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOG.debug("incoming request headers: {}", exchange.getRequest().getHeaders().toString());
        String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZE_TOKEN);
        if(token == null){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        Optional<TokenEntity> tokenEntityOptional = keystoneClient.verifyToken(token);
        if(tokenEntityOptional.isEmpty()){
            LOG.warn("parsed token {} project id failed", token);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        TokenEntity tokenEntity = tokenEntityOptional.get();
        String projectId = tokenEntity.getProjectId();
        LOG.debug("parsed token {} project id success, project id:[{}]", token, projectId);

        // rewrite uri path include project id
        ServerHttpRequest req = exchange.getRequest();
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
        String path = req.getURI().getRawPath();
        path = path.replaceAll(neutronUrlPrefix, "/project/" + projectId);
        path = path.replaceAll(VPC_REPLACE_NAME, VPC_NAME);

        ServerHttpRequest request = req.mutate().path(path).header(TOKEN_INFO_HEADER, tokenEntity.toJson()).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());

        // if log trace enable print the response info
        if(LOG.isTraceEnabled()){
            return chain.filter(exchange.mutate().request(request).build()).then(
                    Mono.fromRunnable(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        LOG.trace("response code {}, headers {}", response.getStatusCode(), response.getHeaders());
                    })
            );
        }
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return KEYSTONE_FILTER_ORDERED;
    }
}
