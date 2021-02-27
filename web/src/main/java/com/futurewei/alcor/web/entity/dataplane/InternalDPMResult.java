package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.schema.Common.ResourceType;
import lombok.Data;

import java.util.List;

@Data
public class InternalDPMResult {
  @JsonProperty("resource_id")
  private String resourceId;

  @JsonProperty("resource_type")
  private ResourceType resourceType;

  @JsonProperty("operation_status")
  private String status;

  @JsonProperty("elapse_time")
  private long elapseTime;

  @JsonProperty("failed_hosts")
  private List<String> failedHosts;

  @JsonProperty("failed_zeta_ports")
  private List<String> failedZetaPorts;

  public InternalDPMResult() {

  }

  public InternalDPMResult(
      String resourceId, ResourceType resourceType, String status, int elapseTime, List<String> failedHosts) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.status = status;
    this.elapseTime = elapseTime;
    this.failedHosts = failedHosts;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getElapseTime() {
    return elapseTime;
  }

  public void setElapseTime(long elapseTime) {
    this.elapseTime = elapseTime;
  }

  public List<String> getFailedHosts() {
    return failedHosts;
  }

  public void setFailedHosts(List<String> failedHosts) {
    this.failedHosts = failedHosts;
  }
}
