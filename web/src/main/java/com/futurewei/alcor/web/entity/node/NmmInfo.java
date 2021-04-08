package com.futurewei.alcor.web.entity.node;

public class NmmInfo {
    public NmmInfo(String ncmId, NcmInfo ncmInfo, NodeInfo nodeInfo) {
        this.ncmId = ncmId;
        this.ncmInfo = ncmInfo;
        this.nodeInfo = nodeInfo;
    }

    public String getNcmId() { return ncmId; }
    public NcmInfo getNcmInfo() { return ncmInfo; }
    public NodeInfo getNodeInfo() { return nodeInfo; }

    public void setNcmId(String ncmId) { this.ncmId = ncmId; }
    public void setNcmInfo(NcmInfo ncmInfo) { this.ncmInfo = ncmInfo; }
    public void setNodeInfo(NodeInfo nodeInfo) { this.nodeInfo = nodeInfo; }

    private String ncmId;
    private NcmInfo ncmInfo;
    private NodeInfo nodeInfo;
}
