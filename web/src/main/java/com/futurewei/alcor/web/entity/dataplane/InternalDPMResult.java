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
