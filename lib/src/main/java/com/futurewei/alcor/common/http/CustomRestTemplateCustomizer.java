package com.futurewei.alcor.common.http;

import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class CustomRestTemplateCustomizer implements RestTemplateCustomizer {

    @ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "true")
    @Override
    public void customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new TracingRestTemplateInterceptor());
        restTemplate.setInterceptors(interceptors);
    }
}
