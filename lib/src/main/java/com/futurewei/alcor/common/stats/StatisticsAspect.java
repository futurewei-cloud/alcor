/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.common.stats;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StatisticsAspect {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsAspect.class);

    @Pointcut("@annotation(com.futurewei.alcor.common.stats.DurationStatistics)")
    public void annotationPointCut() {

    }

    @Around("annotationPointCut()")
    public Object durationStatistics(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = 0;
        long endTime = 0;
        long duration = 0;

        LOG.debug("Calculating duration of {}.{}()...",
                pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());

        startTime = System.nanoTime();

        Object object = pjp.proceed();

        endTime = System.nanoTime();
        duration = endTime- startTime;

        LOG.info("{}.{}() startTime: {}ns, endTime: {}ns, duration: {}ms",
                pjp.getSignature().getDeclaringTypeName(),
                pjp.getSignature().getName(), startTime, endTime, duration/1000000);

        return object;
    }
}
