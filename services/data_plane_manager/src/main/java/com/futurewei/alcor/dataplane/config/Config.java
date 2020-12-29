/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Configuration
public class Config {

  public static final int SHUTDOWN_TIMEOUT = 5;
  @Value("${dataplane.isovs}")
  private String ovs;
  @Value("${dataplane.grpc.port}")
  public int port ;

  @Value("${grpc.min-threads: 100}")
  public int grpcMinThreads;

  @Value("${grpc.max-threads: 200}")
  public int grpcMaxThreads;

  @Value("${grpc.threads-pool-name: grpc-thread-pool}")
  public String grpThreadsName;

  @Value("${protobuf.goal-state-message.version}")
  public int goalStateMessageVersion;

  @Value("${jaeger.host:127.0.0.1}")
  public String jaegerHost;
  @Value("${jaeger.port:5775}")
  public String jaegerPort;
  @Value("${jaeger.flush:1000}")
  public String jaegerFlush;
  @Value("${jaeger.maxQsize:1000}")
  public String jaegerMaxQsize;

  public static FileWriter TIME_STAMP_FILE;
  public static BufferedWriter TIME_STAMP_WRITER;
  public static String LOG_FILE_PATH = "timestamp.log";

  static {
    try {
      File file = new File(LOG_FILE_PATH);
      if (!file.exists()) {
        file.createNewFile();
      }

      TIME_STAMP_FILE = new FileWriter(file);
      TIME_STAMP_WRITER = new BufferedWriter(TIME_STAMP_FILE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getJaegerHost() {
    return jaegerHost;
  }

  public void setJaegerHost(String jaegerHost) {
    this.jaegerHost = jaegerHost;
  }

  public String getJaegerPort() {
    return jaegerPort;
  }

  public void setJaegerPort(String jaegerPort) {
    this.jaegerPort = jaegerPort;
  }

  public String getJaegerFlush() {
    return jaegerFlush;
  }

  public void setJaegerFlush(String jaegerFlush) {
    this.jaegerFlush = jaegerFlush;
  }

  public String getJaegerMaxQsize() {
    return jaegerMaxQsize;
  }

  public void setJaegerMaxQsize(String jaegerMaxQsize) {
    this.jaegerMaxQsize = jaegerMaxQsize;
  }

  public static String PRODUCER_CLIENT_ID = "vpc_controller_p2";

  public String getOvs() {
    return ovs;
  }

  public void setOvs(String ovs) {
    this.ovs = ovs;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
