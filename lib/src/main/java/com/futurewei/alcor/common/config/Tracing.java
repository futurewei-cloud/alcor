package com.futurewei.alcor.common.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import okhttp3.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
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

    public static TracingObj startSpan(Map<String,String> headers, Tracer tracer,String serviceName)
    {
        Tracer.SpanBuilder builder = null;
        SpanContext parentSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
        if (null == parentSpanContext) {
            builder = tracer.buildSpan(serviceName);
        } else {
            builder = tracer.buildSpan(serviceName).asChildOf(parentSpanContext);
        }
        return new TracingObj(builder.start(),headers);
    }

//    public static TracingObj startSpan(HttpServletRequest request, Tracer tracer, String serviceName)
//    {
//
//        Map<String,String> headers=new HashMap();
//        if(request!=null) {
//            Iterator<String> stringIterator = request.getHeaderNames().asIterator();
//            while (stringIterator.hasNext()) {
//                String name = stringIterator.next();
//                String value = request.getHeader(name);
//                headers.put(name, value);
//            }
//            Enumeration<String> headerNames = request.getHeaderNames();
//            while (headerNames.hasMoreElements()) {
//                String header = headerNames.nextElement();
//                headers.put(header, request.getHeader(header));
//            }
//        }
//        Tracer.SpanBuilder builder = null;
//        SpanContext parentSpanContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
//        if (null == parentSpanContext) {
//            builder = tracer.buildSpan(serviceName);
//        } else {
//            builder = tracer.buildSpan(serviceName).asChildOf(parentSpanContext);
//        }
//        return new TracingObj(builder.start(),headers);
//    }
    public static Response StartImpl(String url, String jsonStr, Span span, Tracer tracer, String method) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonStr));
        Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
        Tags.HTTP_METHOD.set(span, method);
        Tags.HTTP_URL.set(span, url);
        tracer.activateSpan(span);
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(request));
        return okHttpClient.newCall(request.build()).execute();
    }
}