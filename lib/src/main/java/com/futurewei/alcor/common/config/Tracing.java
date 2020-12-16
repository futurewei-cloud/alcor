package com.futurewei.alcor.common.config;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import okhttp3.MediaType;
import okhttp3.Request;

import java.util.Iterator;
import java.util.Map;

public final class Tracing {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Span startServerSpan(Tracer tracer, Map<String, String> headers, String operationName) {

        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
            if (parentSpanCtx == null) {
                spanBuilder = tracer.buildSpan(operationName);
            } else {
                spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpanCtx);
            }
        } catch (IllegalArgumentException e) {
            spanBuilder = tracer.buildSpan(operationName);
        }
        // TODO could add more tags like http.url
        return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
    }

    public static TextMap requestBuilderCarrier(final Request.Builder builder) {
        return new TextMap() {
            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                throw new UnsupportedOperationException("not supporting read");
            }

            @Override
            public void put(String key, String value) {
                builder.addHeader(key, value);
            }
        };
    }
}