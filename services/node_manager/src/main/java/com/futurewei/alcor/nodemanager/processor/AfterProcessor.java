package com.futurewei.alcor.nodemanager.processor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterProcessor {
    Class<?>[] value();
}
