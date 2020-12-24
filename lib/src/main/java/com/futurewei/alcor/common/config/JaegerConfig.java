package com.futurewei.alcor.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaegerConfig {
    @Value("${jaeger.host:127.0.0.1}")
    public String jaegerHost;
    @Value("${jaeger.port:5775}")
    public String jaegerPort;
    @Value("${jaeger.flush:1000}")
    public String jaegerFlush;
    @Value("${jaeger.maxQsize:1000}")
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
