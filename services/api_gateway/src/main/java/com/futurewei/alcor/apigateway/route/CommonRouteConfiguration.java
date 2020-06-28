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

package com.futurewei.alcor.apigateway.route;

import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Configuration
public class CommonRouteConfiguration {

    @Bean
    @SuppressWarnings("unchecked")
    public RouteLocator extensionRoute(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/v2.0/extensions")
                    .filters(f -> f.filter((exchange, chain) -> {
                        ServerHttpResponse response = exchange.getResponse();

                        BodyInserter bodyInserter = BodyInserters.fromPublisher(Mono.just("{\"extensions\":{}}"), String.class);
                        CustomerBodyOutputMessage outputMessage = new CustomerBodyOutputMessage(exchange);
                        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                                .then(Mono.defer(() -> {
                                    Flux<DataBuffer> messageBody = outputMessage.getBody();
                                    return response.writeWith(messageBody);
                                }));
                    }, NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER -1))
                .uri("localhost:8080")).build();
    }
}
