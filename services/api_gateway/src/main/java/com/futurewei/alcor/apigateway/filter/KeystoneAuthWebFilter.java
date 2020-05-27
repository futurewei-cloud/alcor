package com.futurewei.alcor.apigateway.filter;

import com.futurewei.alcor.apigateway.utils.KeystoneClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;


@Component
public class KeystoneAuthWebFilter implements WebFilter {

    private static final String AUTHORIZE_TOKEN = "X-Auth-Token";

    @Autowired
    private KeystoneClient keystoneClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZE_TOKEN);
        try {
            String projectId = keystoneClient.verifyToken(token);
            if("".equals(projectId)){
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            // rewrite uri path include project id
            ServerHttpRequest req = exchange.getRequest();
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
            String path = req.getURI().getRawPath();
            String newPath = path.replaceAll("/v2.0", "/project/" + projectId);
            ServerHttpRequest request = req.mutate().path(newPath).build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
            return chain.filter(exchange.mutate().request(request).build());
        } catch (IOException e) {
            exchange.getResponse().setStatusCode(HttpStatus.EXPECTATION_FAILED);
            return exchange.getResponse().setComplete();
        }
    }


}
