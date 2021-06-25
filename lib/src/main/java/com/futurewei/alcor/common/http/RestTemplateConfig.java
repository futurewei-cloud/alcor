package com.futurewei.alcor.common.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class RestTemplateConfig {
    @Bean
    public CustomRestTemplateCustomizer customRestTemplateCustomizer()
    {
        return new CustomRestTemplateCustomizer();
    }

    @Bean
    @DependsOn(value = {"customRestTemplateCustomizer"})
    public RestTemplateBuilder restTemplateBuilder()
    {
        return new RestTemplateBuilder(customRestTemplateCustomizer());
    }
}
