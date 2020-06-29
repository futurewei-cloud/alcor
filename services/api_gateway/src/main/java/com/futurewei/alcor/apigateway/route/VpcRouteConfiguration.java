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

import com.futurewei.alcor.web.entity.vpc.NetworksWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

@Configuration
public class VpcRouteConfiguration {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Bean
    public RouteLocator vpcRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r
                        .path("/*networks", "/*/vpcs")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.modifyResponseBody(VpcsWebJson.class, NetworksWebJson.class,
                                (exchange, vpcsWebJson) -> {
                                    NetworksWebJson networksWebJson = new NetworksWebJson(vpcsWebJson.getVpcs());
                                    return Mono.just(networksWebJson);
                                }))
                        .uri(vpcUrl))
                .route(r -> r
                        .path("/*/networks", "/*/vpcs", "/*/networks/*", "/*/vpcs/*")
                        .uri(vpcUrl))
                .build();
    }
}
