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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InternalDPMResultNB {
  @JsonProperty("resource_id")
  private String resourceId;

  @JsonProperty("resource_type")
  private String resourceType;

  @JsonProperty("operation_status")
  private String status;

  @JsonProperty("elapse_time")
  private int elapseTime;

  public InternalDPMResultNB(
      String resourceId, String resourceType, String status, int elapseTime) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.status = status;
    this.elapseTime = elapseTime;
  }
}
