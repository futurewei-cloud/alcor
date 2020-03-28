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

package com.futurewei.alcor.apigateway.vpc;

import com.futurewei.alcor.apigateway.proxies.VpcManagerServiceProxy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
@EnableConfigurationProperties(VpcWebDestinations.class)
public class VpcWebConfiguration {

    @Bean
    public RouteLocator vpcProxyRouting(RouteLocatorBuilder builder, VpcWebDestinations vpcWebDestinations) {
        return builder.routes()
                .route(r -> r.path("/vpc").and().method("POST").uri(vpcWebDestinations.getVpcManagerServiceUrl()))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderHandlerRouting(VpcWebHandlers vpcWebHandlers) {
        return RouterFunctions.route(GET("/project/{projectId}/vpc/{vpcId}"), vpcWebHandlers::getVpcDetails);
    }

    @Bean
    public VpcWebHandlers vpcManagerHandlers(VpcManagerServiceProxy vpcManagerService) {
        return new VpcWebHandlers(vpcManagerService);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
