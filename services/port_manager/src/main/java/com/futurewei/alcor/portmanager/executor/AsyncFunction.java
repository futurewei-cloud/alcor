package com.futurewei.alcor.portmanager.executor;

@FunctionalInterface
public interface AsyncFunction<T, R> {
    R apply(T var1) throws Exception;
}
