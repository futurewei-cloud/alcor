package com.futurewei.alcor.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaegerConfig {
    @Value("${jaeger.host}")
    public String jaegerHost;
    @Value("${jaeger.port}")
    public String jaegerPort;
    @Value("${jaeger.flush}")
    public String jaegerFlush;
    @Value("${jaeger.maxQsize}")
    public String jaegerMaxQsize;

    public String getJaegerHost() {
        return jaegerHost;
    }

    public void setJaegerHost(String jaegerHost) {
        this.jaegerHost = jaegerHost;
    }

    public String getJaegerPort() {
        return jaegerPort;
    }

    public void setJaegerPort(String jaegerPort) {
        this.jaegerPort = jaegerPort;
    }

    public String getJaegerFlush() {
        return jaegerFlush;
    }

    public String getJaegerMaxQsize() {
        return jaegerMaxQsize;
    }

    public void setJaegerMaxQsize(String jaegerMaxQsize) {
        this.jaegerMaxQsize = jaegerMaxQsize;
    }
}
