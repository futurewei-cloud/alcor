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
    private static final String ELASTICIP_NAME = "elasticips";
    private static final String ELASTICIP_REPLACE_NAME = "floatingips";
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
        path = path.replaceAll(neutronUrlPrefix, "/project/" + projectId)
                .replaceAll(VPC_REPLACE_NAME, VPC_NAME)
                .replaceAll(ELASTICIP_REPLACE_NAME, ELASTICIP_NAME);
        LOG.debug("internal path:[{}]", path);

        ServerHttpRequest request = req.mutate().path(path).header(TOKEN_INFO_HEADER, tokenEntity.toJson()).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
        LOG.debug("internal header:[{}]", request.getHeaders().toString());

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
