package com.futurewei.alcor.common.tracer;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tracer {

    String resource() default "";

}