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
package com.futurewei.alcor.dataplane.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

  // each host_ip should have this amount of gRPC channels.
  @Value("${grpc.number-of-channels-per-host:1}")
  public int numberOfGrpcChannelPerHost;

  // when a channel is set up, send this amount of default GoalStates for warmup.
  @Value("${grpc.number-of-warmups-per-channel:1}")
  public int numberOfWarmupsPerChannel;

  @Value("${grpc.monitor-hosts}")
  public ArrayList<String> monitorHosts;

  @Value("${microservices.netwconfigmanager.service.url}")
  public String netwconfigmanagerHost;

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
