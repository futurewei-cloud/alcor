package com.futurewei.alcor.apigateway.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

public class QuotaRouteConfiguration {

    @Value("${microservices.quota.service.url}")
    private String quotaUrl;

    @Bean
    public RouteLocator quotaGroupLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/*/quotas", "/*/quotas/*",
                        "/project/*/quotas", "/project/*/quotas/*")
                        .uri(quotaUrl))
                .build();
    }
}
