/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
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
