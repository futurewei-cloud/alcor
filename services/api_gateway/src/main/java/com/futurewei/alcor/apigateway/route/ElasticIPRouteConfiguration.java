/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
                .route(r -> r
                        .path("/*/floatingips/*")
                        .and().method(HttpMethod.DELETE)
                        .uri(elasticipUrl))
                .build();
    }
}
