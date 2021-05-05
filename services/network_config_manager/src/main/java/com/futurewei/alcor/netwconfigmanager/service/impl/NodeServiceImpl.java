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
package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.cache.NodeInfoCache;
import com.futurewei.alcor.netwconfigmanager.service.NodeService;
import com.futurewei.alcor.web.entity.node.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;

/**
 * DPM, NMM and now NCM all are holding onto NodeInfo, by making copy of the code
 * instead of sharing. Let this get working first and the re-factor as much as possible into
 * common as shared module/package.
 */
@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger LOG = LoggerFactory.getLogger();
    @Autowired
    private NodeInfoCache nodeInfoCache;

    @Override
    public void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception {
        nodeInfoCache.addNodeInfo(nodeInfoJson.getNodeInfo());
    }

    @Override
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.updateNodeInfo(nodeInfo);
    }

    @Override
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.deleteNodeInfo(nodeId);
    }

    @Override
    public void createNodeInfoBulk(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        List<NodeInfo> nodeInfos = bulkNodeInfoJson.getNodeInfos();
        nodeInfoCache.addNodeInfoBulk(nodeInfos);
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        return nodeInfoCache.getNodeInfo(nodeId);
    }
}