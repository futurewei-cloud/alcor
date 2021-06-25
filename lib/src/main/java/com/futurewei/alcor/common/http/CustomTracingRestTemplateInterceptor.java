package com.futurewei.alcor.common.http;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CustomTracingRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
//        // 创建新的Span，以当前线程中的SpanContext为父，如没有则自己成为根Span
//        try (Scope scope = tracer.buildSpan(httpRequest.getMethod().toString())
//                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).startActive(true)) {
//            // 追踪请求地址
//            scope.span().setTag(Tags.HTTP_URL, httpRequest.getURI().toString())
//            // 将SpanContext注入到请求头中
//            // 看前文中的代码可以知道，服务端通过Tracer.extract可以从请求头中提取出SpanContext
//            tracer.inject(scope.span().context(), Format.Builtin.HTTP_HEADERS,
//                    new HttpHeadersCarrier(httpRequest.getHeaders()));
//
//            // 实际执行请求
//            return execution.execute(httpRequest, body);
//        }
        ClientHttpResponse response = execution.execute(request, body);
        response.getHeaders().add("Foo", "bar");
        return response;
    }
}
