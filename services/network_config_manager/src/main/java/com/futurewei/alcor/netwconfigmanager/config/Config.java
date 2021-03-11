package com.futurewei.alcor.netwconfigmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Configuration
public class Config {

    public static final int SHUTDOWN_TIMEOUT = 5;

    @Value("9014")
    public int servicePort;

    @Value("${dataplane.grpc.port: 5001}")
    public int targetHostPort;

    @Value("${grpc.min-threads: 100}")
    public int grpcMinThreads;

    @Value("${grpc.max-threads: 200}")
    public int grpcMaxThreads;

    @Value("${grpc.threads-pool-name: grpc-thread-pool}")
    public String grpThreadsName;

    @Value("${protobuf.goal-state-message.version: 2}")
    public int goalStateMessageVersion;
}