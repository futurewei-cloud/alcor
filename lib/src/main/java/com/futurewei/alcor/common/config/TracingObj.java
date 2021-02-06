package com.futurewei.alcor.common.config;

import io.opentracing.Span;

import java.util.Map;

public class TracingObj {
    Span span;
    Map<String,String> headers;

    public TracingObj(Span span, Map<String, String> headers) {
        this.span = span;
        this.headers = headers;
    }

    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
