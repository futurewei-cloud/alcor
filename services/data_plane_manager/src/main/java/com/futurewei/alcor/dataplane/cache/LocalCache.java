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

package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;

import java.util.List;

public interface LocalCache {
    void addSubnetPorts(NetworkConfiguration networkConfig) throws Exception;
    void updateSubnetPorts(NetworkConfiguration networkConfig) throws Exception;
    void deleteSubnetPorts(NetworkConfiguration networkConfig);
    InternalSubnetPorts getSubnetPorts(String subnetId) throws Exception;
    void updateLocalCache(NetworkConfiguration networkConfig) throws Exception;

    void addNodeInfo(NodeInfo nodeInfo) throws Exception;
    void addNodeInfoBulk(List<NodeInfo> nodeInfos) throws Exception;
    void updateNodeInfo(NodeInfo nodeInfo) throws Exception;
    void deleteNodeInfo(String nodeId) throws Exception;
    NodeInfo getNodeInfo(String nodeId) throws Exception;
    List<NodeInfo> getNodeInfoByNodeIp(String nodeIp) throws Exception;
}
