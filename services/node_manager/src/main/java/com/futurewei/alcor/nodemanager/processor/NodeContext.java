package com.futurewei.alcor.nodemanager.processor;

import com.futurewei.alcor.nodemanager.request.RequestManager;
import com.futurewei.alcor.web.entity.node.NodeInfo;

import java.util.List;

public class NodeContext {
    private NodeInfo nodeInfo;
    private List<NodeInfo> nodeInfos;
    private RequestManager requestManager;

    public NodeContext(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.nodeInfos = null;
        this.requestManager = new RequestManager();
    }

    public NodeContext(List<NodeInfo> nodeInfos) {
        this.nodeInfo = null;
        this.nodeInfos = nodeInfos;
        this.requestManager = new RequestManager();
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }
}
