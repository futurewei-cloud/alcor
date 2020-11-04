package com.futurewei.alcor.web.entity.node;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BulkNodeInfoJson {
  @JsonProperty("host_infos")
  private List<NodeInfo> nodeInfos;

  public BulkNodeInfoJson(List<NodeInfo> nodeInfos) {
    this.nodeInfos = nodeInfos;
  }

  public BulkNodeInfoJson() {}

  public List<NodeInfo> getNodeInfos() {
    return nodeInfos;
  }

  public void setNodeInfos(List<NodeInfo> nodeInfos) {
    this.nodeInfos = nodeInfos;
  }
}
