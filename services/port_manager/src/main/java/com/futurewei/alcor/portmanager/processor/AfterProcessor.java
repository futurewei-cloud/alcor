package com.futurewei.alcor.portmanager.processor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterProcessor {
    Class<?>[] value();
}
