package com.futurewei.alcor.portmanager.config;

public class ThreadPoolExecutorConfig {
    //Core thread pool size
    public static int corePoolSize = 10;

    //Maximum thread pool size
    public static int maximumPoolSize = 20;

    //Maximum idle time of thread
    public static int KeepAliveTime = 5000;

    //Queue size of tasks waiting to be executed
    public static int capacity = 1024;

}
