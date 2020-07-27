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

package com.futurewei.alcor.apigateway.vpc;

import com.futurewei.alcor.apigateway.proxies.VpcManagerServiceProxy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Deprecated
//@Configuration
//@EnableConfigurationProperties(VpcWebDestinations.class)
public class VpcWebConfiguration {

    @Bean
    public RouterFunction<ServerResponse> vpcHandlerRouting(VpcWebHandlers vpcWebHandlers) {
        return
                route(GET("/project/{projectId}/vpcs/{vpcId}"), vpcWebHandlers::getVpc)
                        .andRoute(GET("/project/{projectId}/network/{vpcId}"), vpcWebHandlers::getVpc)
                        .andRoute(POST("/project/{projectId}/vpcs").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), vpcWebHandlers::createVpc)
                        .andRoute(POST("/project/{projectId}/networks").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), vpcWebHandlers::createVpc)
                        .andRoute(DELETE("/project/{projectId}/vpcs/{vpcId}"), vpcWebHandlers::deleteVpc)
                        .andRoute(DELETE("/project/{projectId}/networks/{vpcId}"), vpcWebHandlers::deleteVpc);
    }

    @Bean
    public RouterFunction<ServerResponse> vpcHandlerDebugRouting(VpcWebHandlers vpcWebHandlers) {
        return route(GET("/vipmgr/actuator/health"), vpcWebHandlers::getVpcManagerHealthStatus);
    }

    @Bean
    public VpcWebHandlers vpcWebHandlers(VpcManagerServiceProxy vpcManagerServiceProxy) {
        return new VpcWebHandlers(vpcManagerServiceProxy);
    }

    @Bean
    public WebClient vpcManagerWebClient() {
        return WebClient.create();
    }

}
