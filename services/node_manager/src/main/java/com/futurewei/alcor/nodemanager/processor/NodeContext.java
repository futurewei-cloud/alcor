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
package com.futurewei.alcor.nodemanager.processor;

import com.futurewei.alcor.nodemanager.request.RequestManager;
import com.futurewei.alcor.web.entity.node.NodeInfo;

import java.util.List;

public class NodeContext {
    private String nodeId;
    private NodeInfo nodeInfo;
    private List<NodeInfo> nodeInfos;
    private RequestManager requestManager;

    public NodeContext(String nodeId) {
        this.nodeId = nodeId;
        this.nodeInfo = null;
        this.nodeInfos = null;
        this.requestManager = new RequestManager();
    }

    public NodeContext(NodeInfo nodeInfo) {
        this.nodeId = nodeInfo.getId();
        this.nodeInfo = nodeInfo;
        this.nodeInfos = null;
        this.requestManager = new RequestManager();
    }

    public NodeContext(List<NodeInfo> nodeInfos) {
        this.nodeId = null;
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
