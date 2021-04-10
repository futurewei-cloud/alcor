/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
    public RouteLocator routeRouteLocator(RouteLocatorBuilder builder){
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
