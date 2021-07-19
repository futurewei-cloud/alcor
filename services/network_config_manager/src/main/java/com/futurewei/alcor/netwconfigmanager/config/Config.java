/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.netwconfigmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Configuration
public class Config {

    public static final int SHUTDOWN_TIMEOUT = 5;

    @Value("${server.port: 9014}")
    public int servicePort;

    @Value("${dataplane.grpc.port: 50001}")
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