/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.common.tracer;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class TracerAspect {

    private final Tracer tracer;

    public TracerAspect(@Autowired Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(com.futurewei.alcor.common.tracer.Tracer)")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return performTracing(joinPoint, false);
    }

    private Object performTracing(ProceedingJoinPoint joinPoint, boolean origin) throws Throwable {
        Object proceed;
        final Signature signature = joinPoint.getSignature();
        //final String className = StringUtils.substringAfterLast(signature.getDeclaringTypeName(), ".");
        final String className = signature.getDeclaringTypeName();
        final String operationName = className + ":" + signature.getName();
        Span pSpan = tracer.activeSpan();
        Span span;
        if (pSpan != null) {
            span = tracer.buildSpan(operationName).asChildOf(pSpan.context()).start();
        } else {
            span = tracer.buildSpan(operationName).start();
        }
        try (Scope scope = tracer.scopeManager().activate(span)) {
            span.setTag(Tags.COMPONENT.getKey(), className);
            span.setTag(Tags.SPAN_KIND.getKey(), Arrays.toString(joinPoint.getArgs()));
            proceed = joinPoint.proceed();
        }
        return proceed;
    }
}
