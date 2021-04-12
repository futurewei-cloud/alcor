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
                        .path("/*/networks", "/*/vpcs", "/project/*/vpcs")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.modifyResponseBody(VpcsWebJson.class, NetworksWebJson.class,
                                (exchange, vpcsWebJson) -> {
                                    NetworksWebJson networksWebJson = new NetworksWebJson(vpcsWebJson.getVpcs());
                                    return Mono.just(networksWebJson);
                                }))
                        .uri(vpcUrl))
                .route(r -> r
                        .path("/*/networks", "/*/networks/*",
                                "/project/*/vpcs",  "/project/*/vpcs/*")
                        .uri(vpcUrl))
                .build();
    }
}
