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

import com.futurewei.alcor.apigateway.filter.KeystoneAuthGwFilter;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfoWrapper;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpsInfoWrapper;
import com.futurewei.alcor.web.entity.elasticip.openstack.*;
import com.futurewei.alcor.web.entity.vpc.NetworksWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ElasticIPRouteConfiguration {

    @Value("${microservices.elasticip.service.url}")
    private String elasticipUrl;

    private static class FloatingIpGetsFilter implements Function<GatewayFilterSpec, UriSpec> {
         public UriSpec apply(GatewayFilterSpec t) {
            return t.modifyResponseBody(ElasticIpsInfoWrapper.class, FloatingIpsWrapper.class,
                    (exchange, elasticIpsInfoWrapper) -> {
                        List<ElasticIpInfo> elasticIpInfoList = elasticIpsInfoWrapper.getElasticips();
                        List<FloatingIpResponse> floatingIpList = new LinkedList<>();
                        if (elasticIpInfoList != null) {
                            for (ElasticIpInfo item: elasticIpInfoList) {
                                floatingIpList.add(new FloatingIpResponse(item));
                            }
                        }
                        FloatingIpsWrapper floatingIpsWrapper = new FloatingIpsWrapper(floatingIpList);
                        return Mono.just(floatingIpsWrapper);
                    });
        }
    }

    private static class FloatingIpGetFilter implements Function<GatewayFilterSpec, UriSpec> {
        public UriSpec apply(GatewayFilterSpec t) {
            return t.modifyResponseBody(ElasticIpInfoWrapper.class, FloatingIpResponseWrapper.class,
                    (exchange, elasticIpInfoWrapper) -> {
                        FloatingIpResponse floatingIpResponse = null;
                        if (elasticIpInfoWrapper.getElasticip() != null) {
                            floatingIpResponse = new FloatingIpResponse(elasticIpInfoWrapper.getElasticip());
                        }
                        return Mono.just(new FloatingIpResponseWrapper(floatingIpResponse));
                    });
        }
    }

    private static class FloatingIpUpdateFilter implements Function<GatewayFilterSpec, UriSpec> {
        public UriSpec apply(GatewayFilterSpec t) {
            return t.modifyRequestBody(FloatingIpRequestWrapper.class, ElasticIpInfoWrapper.class,
                    (exchange, floatingIpRequestWrapper) -> {
                        ElasticIpInfo elasticIpInfo = null;
                        FloatingIpRequest floatingIpRequest = floatingIpRequestWrapper.getFloatingIpRequest();
                        if (floatingIpRequest != null) {
                            elasticIpInfo = floatingIpRequest.getElasticIpInfo();
                        }

                        return Mono.just(new ElasticIpInfoWrapper(elasticIpInfo));
                    }).modifyResponseBody(ElasticIpInfoWrapper.class, FloatingIpResponseWrapper.class,
                    (exchange, elasticIpInfoWrapper) -> {
                        FloatingIpResponse floatingIpResponse = null;
                        if (elasticIpInfoWrapper.getElasticip() != null) {
                            floatingIpResponse = new FloatingIpResponse(elasticIpInfoWrapper.getElasticip());
                        }
                        return Mono.just(new FloatingIpResponseWrapper(floatingIpResponse));
                    });
        }
    }

    @Bean
    public RouteLocator elasticIpRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r
                        .path("/project/*/elasticips", "/project/*/elasticips/*",
                                "/elasticip-ranges",  "/elasticip-ranges/*")
                        .uri(elasticipUrl))
                .route(r -> r
                        .path("/*/floatingips")
                        .and().method(HttpMethod.GET)
                        .filters(new FloatingIpGetsFilter())
                        .uri(elasticipUrl))
                .route(r -> r
                        .path("/*/floatingips/*")
                        .and().method(HttpMethod.GET)
                        .filters(new FloatingIpGetFilter())
                        .uri(elasticipUrl))
                .route(r -> r
                        .path("/*/floatingips")
                        .and().method(HttpMethod.POST)
                        .filters(new FloatingIpUpdateFilter())
                        .uri(elasticipUrl))
                .route(r -> r
                        .path("/*/floatingips/*")
                        .and().method(HttpMethod.PUT)
                        .filters(new FloatingIpUpdateFilter())
                        .uri(elasticipUrl))
                .build();
    }
}
