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
    public RouterFunction<ServerResponse> vpcHandlerRouting(VpcWebHandlers vpcWebHandlers) {
        return RouterFunctions.route(GET("/project/{projectId}/vpc/{vpcId}"), vpcWebHandlers::getVpcDetails);
    }

    @Bean
    public RouterFunction<ServerResponse> vpcHandlerDebugRouting(VpcWebHandlers vpcWebHandlers) {
        return RouterFunctions.route(GET("/vipmgr/actuator/health"), vpcWebHandlers::getVpcManagerHealthStatus);
    }

    @Bean
    public VpcWebHandlers vpcWebHandlers(VpcManagerServiceProxy vpcManagerService) {
        return new VpcWebHandlers(vpcManagerService);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

//    @Bean
//    public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
//        String httpUri = uriConfiguration.getHttpbin();
//        return builder.routes()
//                .route(p -> p
//                        .path("/get")
//                        .filters(f -> f.addRequestHeader("Hello", "World"))
//                        .uri(httpUri))
//                .route(p -> p
//                        .host("*.hystrix.com")
//                        .filters(f -> f
//                                .hystrix(config -> config
//                                        .setName("mycmd")
//                                        .setFallbackUri("forward:/fallback")))
//                        .uri(httpUri))
//                .build();
//    }

    // tag::fallback[]
//    @RequestMapping("/fallback")
//    public Mono<String> fallback() {
//        return Mono.just("fallback");
//    }
//    // end::fallback[]
}
