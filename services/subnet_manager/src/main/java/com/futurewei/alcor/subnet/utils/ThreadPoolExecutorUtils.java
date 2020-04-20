package com.futurewei.alcor.subnet.utils;

import com.futurewei.alcor.subnet.config.ThreadPoolExecutorConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorUtils {
    public static final ThreadPoolExecutor SELECT_POOL_EXECUTOR = new ThreadPoolExecutor(
            ThreadPoolExecutorConfig.corePoolSize,
            ThreadPoolExecutorConfig.maximumPoolSize,
            ThreadPoolExecutorConfig.KeepAliveTime,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(ThreadPoolExecutorConfig.capacity),
            new ThreadFactoryBuilder().setNameFormat("selectThreadPoolExecutor-%d").build());
}
