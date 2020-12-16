package com.futurewei.alcor.common.config;


import okhttp3.Request;

import java.util.Iterator;
import java.util.Map;

public class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {
    private final Request.Builder builder;

    public RequestBuilderCarrier(Request.Builder builder) {
        this.builder = builder;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("for now we don't support read");
    }

    @Override
    public void put(String key, String value) {
        builder.addHeader(key, value);
    }
}