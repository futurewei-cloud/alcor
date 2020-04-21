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

package com.futurewei.alcor.apigateway.subnet;

import com.futurewei.alcor.apigateway.proxies.SubnetManagerServiceProxy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableConfigurationProperties(SubnetWebDestinations.class)
public class SubnetWebConfiguration {

    @Bean
    public RouterFunction<ServerResponse> subnetHandlerRouting(SubnetWebHandlers subnetWebHandlers) {
        return
                route(GET("/project/{projectId}/subnets/{subnetId}"), subnetWebHandlers::getSubnet)
                        .andRoute(POST("/project/{projectId}/subnets").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), subnetWebHandlers::createSubnet)
                        .andRoute(PUT("/project/{projectId}/subnets/{subnetId}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), subnetWebHandlers::updateSubnet)
                        .andRoute(DELETE("/project/{projectId}/subnets/{subnetId}"), subnetWebHandlers::deleteSubnet)
                        .andRoute(GET("/project/{projectId}/subnets"), subnetWebHandlers::getSubnets);
//                        .addRoute(POST("/project/{projectId}/subnets"), subnetWebHandlers::bulkCreateSubnet);
    }

    @Bean
    public RouterFunction<ServerResponse> subnetHandlerDebugRouting(SubnetWebHandlers subnetWebHandlers) {
        return route(GET("/subnetmgr/actuator/health"), subnetWebHandlers::getSubnetManagerHealthStatus);
    }

    @Bean
    public SubnetWebHandlers subnetWebHandlers(SubnetManagerServiceProxy subnetManagerServiceProxy) {
        return new SubnetWebHandlers(subnetManagerServiceProxy);
    }

    @Bean
    public WebClient subnetManagerWebClient() {
        return WebClient.create();
    }

}
