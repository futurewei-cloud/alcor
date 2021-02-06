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

import com.futurewei.alcor.common.config.JaegerTracerHelper;
import com.futurewei.alcor.common.config.Tracing;
import com.futurewei.alcor.common.config.TracingObj;
import com.futurewei.alcor.web.entity.vpc.NetworksWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.contrib.spring.web.client.HttpHeadersCarrier;
import io.opentracing.propagation.Format;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class VpcRouteConfiguration {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Value("${jaeger.host:127.0.0.1}")
    public String jaegerHost;
    @Value("${jaeger.port:5775}")
    public int jaegerPort;
    @Value("${jaeger.flush:1000}")
    public int jaegerFlush;
    @Value("${jaeger.maxQsize:1000}")
    public int jaegerMaxQsize;

    @Bean
    public RouteLocator vpcRouteLocator(RouteLocatorBuilder builder) {
        String serviceName = "ApiGW";

        return builder.routes()
                .route(r -> r
                        .path("/*/networks", "/*/vpcs", "/project/*/vpcs")
                        .and().method(HttpMethod.GET)
                        .filters(f ->
                                {
                                    f.modifyResponseBody(VpcsWebJson.class, NetworksWebJson.class,
                                            (exchange, vpcsWebJson) -> {
                                                NetworksWebJson networksWebJson = new NetworksWebJson(vpcsWebJson.getVpcs());
                                                return Mono.just(networksWebJson);
                                            });
// this would not work since they need String,String as input while Span has header as Component
//                                    f.addRequestHeader()
                                    Map<String, String> httpHeaders = new HashMap<>();
                                    try (JaegerTracer tracer = new JaegerTracerHelper().initTracer(serviceName, jaegerHost, jaegerPort, jaegerFlush, jaegerMaxQsize)) {
                                        TracingObj tracingObj = Tracing.startSpan(httpHeaders, tracer, serviceName);
                                        Span span = tracingObj.getSpan();
//                                        Span span = this.tracer.buildSpan(path(builder))
//                                                .asChildOf(tracer.activeSpan())
//                                                .withTag(Tags.COMPONENT.getKey(), COMPONENT)
//                                                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
//                                                .withTag(ROUTE_ID, getRouteId(exchange))
//                                                .start();
                                        HttpHeaders headersWithInput = new HttpHeaders();
                                        try {
                                            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new HttpHeadersCarrier(headersWithInput));
                                        } catch (Exception ignore) {
                                            ignore.printStackTrace();
                                        }
                                        headersWithInput.addAll(headersWithInput);
//                                        addHeadersWithInput((ServerHttpRequest.Builder)builder, headersWithInput);
                                    }
                                    return f;

                                }
                        )
                        .uri(vpcUrl))
                .route(r -> r
                        .path("/*/networks", "/*/networks/*",
                                "/project/*/vpcs", "/project/*/vpcs/*")
                        .uri(vpcUrl))
                .build();
    }

    private void addHeadersWithInput(ServerHttpRequest.Builder builder,
                                     HttpHeaders headersWithInput) {
        for (Map.Entry<String, List<String>> entry : builder.build().getHeaders()
                .entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            headersWithInput.put(key, value);
        }
    }
}
