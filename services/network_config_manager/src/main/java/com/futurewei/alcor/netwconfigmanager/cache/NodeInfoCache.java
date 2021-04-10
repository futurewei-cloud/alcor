/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * NodeInfo maintained by NCM. Validation is not necessary since NMM/DPM will have
 * already done all the validation
 */
@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeInfoCache {
    private ICache<String, NodeInfo> nodeInfoCache;
    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    public NodeInfoCache(CacheFactory cacheFactory) {
        nodeInfoCache = cacheFactory.getCache(NodeInfo.class);
    }

    @DurationStatistics
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        NodeInfo nodeInfo = nodeInfoCache.get(nodeId);

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
}
