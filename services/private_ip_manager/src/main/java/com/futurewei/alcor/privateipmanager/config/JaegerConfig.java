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
package com.futurewei.alcor.privateipmanager.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaegerConfig {

  @Value("${jaeger.host:127.0.0.1}")
  public String jaegerHost;
  @Value("${jaeger.port:5775}")
  public int jaegerPort;
  @Value("${jaeger.flush:1000}")
  public int jaegerFlush;
  @Value("${jaeger.maxQsize:1000}")
  public int jaegerMaxQsize;

  public String getJaegerHost() {
    return jaegerHost;
  }

  public void setJaegerHost(String jaegerHost) {
    this.jaegerHost = jaegerHost;
  }

  public int getJaegerPort() {
    return jaegerPort;
  }

  public void setJaegerPort(int jaegerPort) {
    this.jaegerPort = jaegerPort;
  }

  public int getJaegerFlush() {
    return jaegerFlush;
  }

  public void setJaegerFlush(int jaegerFlush) {
    this.jaegerFlush = jaegerFlush;
  }

  public int getJaegerMaxQsize() {
    return jaegerMaxQsize;
  }

  public void setJaegerMaxQsize(int jaegerMaxQsize) {
    this.jaegerMaxQsize = jaegerMaxQsize;
  }
}
