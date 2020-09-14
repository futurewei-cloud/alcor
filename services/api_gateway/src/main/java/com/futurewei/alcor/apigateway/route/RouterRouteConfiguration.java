package com.futurewei.alcor.apigateway.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterRouteConfiguration {
    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Bean
    public RouteLocator vpcRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/*/routers", "/*/routers/*",
                        "/*/routers/*/add_router_interface", "/*/routers/*/remove_router_interface",
                        "/*/routers/*/add_extra_routes", "/*/routers/*/remove_extra_routes",
                        "/project/*/routers", "/project/*/routers/*",
                        "/project/*/routers/*/add_router_interface", "/project/*/routers/*/remove_router_interface",
                        "/project/*/routers/*/add_extra_routes", "/project/*/routers/*/remove_extra_routes")
                        .uri(routeUrl))
                .build();
    }
}
