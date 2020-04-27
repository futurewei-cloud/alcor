/*Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.nodemanager.service.implement;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.nodemanager.dao.file.DataCenterConfigLoader;
import com.futurewei.alcor.nodemanager.dao.repository.NodeRepository;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);
    static public Hashtable<String, NodeInfo> Nodes = new Hashtable<String, NodeInfo>();

    @Value("${nodemanager.nodeinfo.location}")
    private String nodeInfoLocation;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private DataCenterConfigLoader dataCenterConfigLoader;

    @Override
    public NodeInfo getNodeInfoById(String hostId) throws Exception {
        NodeInfo nodeInfo = null;
        try {
            nodeInfo = nodeRepository.findItem(hostId);
        } catch (Exception e) {
            throw e;
        }
        return nodeInfo;
    }

    @Override
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception {
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public NodeInfo updateNodeInfo(String hostId, NodeInfo nodeInfo) throws Exception {
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public String deleteNodeInfo(String hostId) throws Exception {
        if (hostId == null)
            throw (new ParameterNullOrEmptyException(NodeUtil.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            nodeRepository.deleteItem(hostId);
        } catch (Exception e) {
            throw e;
        }
        return hostId;
    }
}
