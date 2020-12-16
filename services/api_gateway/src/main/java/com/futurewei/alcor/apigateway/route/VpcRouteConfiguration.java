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

import com.futurewei.alcor.common.config.RequestBuilderCarrier;
import com.futurewei.alcor.web.entity.vpc.NetworksWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import com.futurewei.alcor.common.config.JaegerTracerHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import okhttp3.*;
import com.futurewei.alcor.common.config.Tracing;

@Configuration
public class VpcRouteConfiguration {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    @Autowired
    HttpServletRequest request;

    @Bean
    public RouteLocator vpcRouteLocator(RouteLocatorBuilder builder){
        String serviceName="ApiGW";
        Map<String,String> headers=new HashMap();
        Iterator<String> stringIterator = request.getHeaderNames().asIterator();
        while(stringIterator.hasNext())
        {
            String name = stringIterator.next();
            String value=request.getHeader(name);
            headers.put(name,value);
        }
        Tracer tracer = new JaegerTracerHelper().initTracer(serviceName);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        Tracer.SpanBuilder spanBuilder = null;
        SpanContext parentSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
        if (null == parentSpanContext) {
            spanBuilder = tracer.buildSpan(serviceName);
        } else {
            spanBuilder = tracer.buildSpan("apiGW").asChildOf(parentSpanContext);
        }
        Span span = spanBuilder.start();
        Request.Builder request2 = new Request.Builder().url(vpcUrl).post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), ""));

        Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
        Tags.HTTP_URL.set(span, vpcUrl);
        tracer.activateSpan(span);
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(request2));


        try (Scope op= tracer.scopeManager().activate(span)) {
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
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        finally
        {
            span.finish();
        }

        return null;
    }
}
