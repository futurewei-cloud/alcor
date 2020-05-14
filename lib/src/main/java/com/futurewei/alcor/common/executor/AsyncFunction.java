package com.futurewei.alcor.common.executor;

@FunctionalInterface
public interface AsyncFunction<T, R> {
    R apply(T var1) throws Exception;
}
