package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeInfoCache {
    private ICache<String, NodeInfo> nodeInfoCache;

    @Autowired
    private NodeManagerRestClient nodeManagerRestClient;

    @Autowired
    public NodeInfoCache(CacheFactory cacheFactory) {
        nodeInfoCache = cacheFactory.getCache(NodeInfo.class);
    }


    @DurationStatistics
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        NodeInfo nodeInfo;
        try {
            nodeInfo = nodeInfoCache.get(nodeId);
            assert nodeInfo != null;
        } catch (Exception e) {
            NodeInfo newNodeInfo = nodeManagerRestClient.getNodeInfo(nodeId).getNodeInfo();
            nodeInfoCache.put(newNodeInfo.getId(), newNodeInfo);
            nodeInfo = newNodeInfo;
        }
        return nodeInfo;
    }


    @DurationStatistics
    public void addNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.remove(nodeId);
    }

    public List<NodeInfo> getNodeInfoByNodeIp(String nodeIp) throws Exception {
        List<NodeInfo> result = new ArrayList<>();

        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = nodeIp;
        queryParams.put("localIp", values);
        Map<String, NodeInfo> nodeInfoMap = nodeInfoCache.getAll(queryParams);

        if (nodeInfoMap == null) {
            return result;
        }

        for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
            NodeInfo nodeInfo = new NodeInfo(entry.getValue());
            result.add(nodeInfo);
        }
        return result;
    }
}
