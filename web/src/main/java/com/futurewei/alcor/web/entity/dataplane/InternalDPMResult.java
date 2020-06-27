package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InternalDPMResult {
  @JsonProperty("resource_id")
  private String resourceId;

  @JsonProperty("resource_type")
  private String resourceType;

  @JsonProperty("operation_status")
  private String status;

  @JsonProperty("elapse_time")
  private int elapseTime;

  public InternalDPMResult(
      String resourceId, String resourceType, String status, int elapseTime) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.status = status;
    this.elapseTime = elapseTime;
  }
}
