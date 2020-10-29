package com.futurewei.alcor.common.executor;

public class ThreadPoolExecutorConfig {
    //Core thread pool size
    public static int corePoolSize = 32;

    //Maximum thread pool size
    public static int maximumPoolSize = 128;

    //Maximum idle time of thread
    public static int KeepAliveTime = 5000;

    //Queue size of tasks waiting to be executed
    public static int capacity = 1024;

}
