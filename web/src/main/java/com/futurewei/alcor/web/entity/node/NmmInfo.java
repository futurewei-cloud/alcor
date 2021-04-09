package com.futurewei.alcor.web.entity.node;

import java.util.List;

public class NmmInfo {
    public NmmInfo(String ncmId, NcmInfo ncmInfo, List<String> nodeIds) {
        this.ncmId = ncmId;
        this.ncmInfo = ncmInfo;
        this.nodeIds = nodeIds;
    }

    public String getNcmId() { return ncmId; }
    public NcmInfo getNcmInfo() { return ncmInfo; }
    public List<String> getNodeIds() { return nodeIds; }

    public void setNcmId(String ncmId) { this.ncmId = ncmId; }
    public void setNcmInfo(NcmInfo ncmInfo) { this.ncmInfo = ncmInfo; }
    public void addNodeId(NodeInfo nodeInfo) { addNodeId(nodeInfo.getId()); }
    public void addNodeId(String nodeId) { this.nodeIds.add(nodeId); }
    public void addNodeIds(List<String> nodeIds) { this.nodeIds.addAll(nodeIds); }

    private String ncmId;
    private NcmInfo ncmInfo;
    private List<String> nodeIds;
}