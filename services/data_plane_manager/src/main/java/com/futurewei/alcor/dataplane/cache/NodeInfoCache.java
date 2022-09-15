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

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.dataplane.exception.NodeInfoNotFound;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeInfoCache {
    private ICache<String, NodeInfo> nodeInfoCache;

    @Autowired
    private NodeManagerRestClient nodeManagerRestClient;

    @Autowired
    public NodeInfoCache(CacheFactory cacheFactory) {
        nodeInfoCache = cacheFactory.getCache(NodeInfo.class, "dpm_nodeinfo_cache");
    }


    @DurationStatistics
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        NodeInfo nodeInfo = null;

        nodeInfo = nodeInfoCache.get(nodeId);

        if (nodeInfo == null) {
            try {
                NodeInfo newNodeInfo = nodeManagerRestClient.getNodeInfo(nodeId).getNodeInfo();
                if (newNodeInfo == null) {
                    throw new NodeInfoNotFound("Could not get corresponding node with NodeId: " + nodeId + "from NodeManager");
                }
                nodeInfoCache.put(newNodeInfo.getId(), newNodeInfo);
                nodeInfo = newNodeInfo;
            } catch (Exception e) {

            }
        }
        return nodeInfo;
    }


    @DurationStatistics
    public void addNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void addNodeInfoBulk(List<NodeInfo> nodeInfos) throws Exception {
        Map<String, NodeInfo> nodeInfoMap = nodeInfos.stream().collect(Collectors.toMap(NodeInfo::getId, Function.identity()));
        nodeInfoCache.putAll(nodeInfoMap);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.remove(nodeId);
    }

    @DurationStatistics
    public List<NodeInfo> getNodeInfoByNodeIp(String nodeIp) throws Exception {
            List<NodeInfo> result = new ArrayList<>();

            Map<String, Object[]> queryParams = new HashMap<>();
            Object[] values = new Object[1];
            values[0] = nodeIp;
            queryParams.put("localIp", values);
            Map<String, NodeInfo> nodeInfoMap = nodeInfoCache.getAll(queryParams);

            if (nodeInfoMap.size() == 0) {
                result = nodeManagerRestClient.getNodeInfoByNodeIp(nodeIp);
                if (result == null || result.size() == 0) {
                    throw new NodeInfoNotFound("Could not get corresponding node with NodeIp: " + nodeIp + " from NodeManager");
                }
                for (NodeInfo nodeInfo : result) {
                    nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
                }
                return result;
            }

            for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
                NodeInfo nodeInfo = new NodeInfo(entry.getValue());
                result.add(nodeInfo);
            }
            return result;
    }
}
