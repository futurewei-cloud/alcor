package com.futurewei.alcor.subnet.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorUtils {
    public static final ThreadPoolExecutor SELECT_POOL_EXECUTOR = new ThreadPoolExecutor(10, 20, 5000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024),
            new ThreadFactoryBuilder().setNameFormat("selectThreadPoolExecutor-%d").build());
}
