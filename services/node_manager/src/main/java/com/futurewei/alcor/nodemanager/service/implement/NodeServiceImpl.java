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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.exception.NodeRepositoryException;
import com.futurewei.alcor.nodemanager.processor.IProcessor;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.nodemanager.processor.ProcessorManager;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeRepository nodeRepository;

    private void handleCreateNodeRequest(NodeInfo nodeInfo) {
        NodeContext nodeContext = new NodeContext(nodeInfo);
        IProcessor processorChain = ProcessorManager.getProcessChain();
        try {
            processorChain.createNode(nodeContext);
            nodeContext.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {

        }

    }

    private void handleUpdateNodeRequest(NodeInfo nodeInfo) {

    }

    private void handleDeleteNodeRequest(String nodeId) {

    }

    private void handleCreateNodeBulkRequest(List<NodeInfo> nodeInfo) {

    }

    /**
     * read bulk nodes' information from file
     *
     * @param file nodes' file, e.g.) machine.json
     * @return total nodes number
     * @throws IOException file read exception, NodeRepositoryException exception caused by Repository
     */
    @DurationStatistics
    public int getNodeInfoFromUpload(MultipartFile file) throws IOException, NodeRepositoryException, Exception {
        String strMethodName = "getNodeInfoFromUpload";
        int nReturn = 0;
        List<NodeInfo> nodeList = new ArrayList<NodeInfo>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            NodeFileLoader dataCenterConfigLoader = new NodeFileLoader();
            nodeList = dataCenterConfigLoader.getHostNodeListFromUpload(reader);
            if (nodeList != null) {
                nodeRepository.addItemBulkTransaction(nodeList);
                nReturn = nodeList.size();
            }
        } catch (IOException e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        catch (CacheException e) {
            logger.error(strMethodName+e.getMessage());
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return nReturn;
    }

    /**
     * get a node's information from repository
     *
     * @param nodeId node's id
     * @return node's information
     * @throws IOException NodeRepositoryException exception caused by Repository
     */
    @Override
    @DurationStatistics
    public NodeInfo getNodeInfoById(String nodeId) throws NodeRepositoryException, Exception {
        String strMethodName = "getNodeInfoById";
        if (nodeId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo nodeInfo = null;
        try {
            nodeInfo = nodeRepository.findItem(nodeId);
        } catch (CacheException e) {
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }catch (Exception e)
        {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return nodeInfo;
    }

    /**
     * get all nodes information from repository
     *
     * @param
     * @return nodes list
     * @throws IOException NodeRepositoryException exception caused by Repository
     */
    @Override
    @DurationStatistics
    public List<NodeInfo> getAllNodes() throws Exception {
        String strMethodName = "getAllNodes";
        List<NodeInfo> nodes = new ArrayList<NodeInfo>();
        try {
            nodes = new ArrayList(nodeRepository.findAllItems().values());
        } catch (CacheException e) {
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }catch (Exception e)
        {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return nodes;
    }

    /**
     *  get all nodes info filtered by params
     * @param queryParams
     * @return List<NodeInfo>
     * @throws Exception
     */
    @Override
    @DurationStatistics
    public List<NodeInfo> getAllNodes(Map<String, Object[]> queryParams) throws Exception {
        List<NodeInfo> result = new ArrayList<>();

        Map<String, NodeInfo> nodeInfoMap = nodeRepository.findAllItems(queryParams);
        if (nodeInfoMap == null) {
            return result;
        }

        for (Map.Entry<String, NodeInfo> entry: nodeInfoMap.entrySet()) {
            NodeInfo nodeInfo = new NodeInfo(entry.getValue());
            result.add(nodeInfo);
        }

        return result;
    }

    /**
     * create a new node information
     *
     * @param nodeInfo new node's information
     * @return new node's information
     * @throws ParameterNullOrEmptyException node information input is not valid, NodeRepositoryException exception caused by Repository
     */
    @Override
    @DurationStatistics
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) throws ParameterNullOrEmptyException, NodeRepositoryException,Exception {
        String strMethodName = "createNodeInfo";
        if (nodeInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeInfo.getId());
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
                handleCreateNodeRequest(nodeInfo);

            } catch (CacheException e) {
                logger.error(strMethodName+e.getMessage());
                throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
            }catch (Exception e)
            {
                logger.error(strMethodName+e.getMessage());
                throw e;
            }
        }
        return nodeInfo;
    }

    /**
     * create new nodes information in bulk
     *
     * @param nodeInfo new node's information in bulk
     * @return List of new node's information
     */
    @Override
    public List<NodeInfo> createNodeInfoBulk(List<NodeInfo> nodeInfo) throws Exception {
        String strMethodName = "createNodeInfoBulk";
        if (nodeInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (nodeInfo != null) {
            try {
                nodeRepository.addItemBulkTransaction(nodeInfo);
                handleCreateNodeBulkRequest(nodeInfo);
            } catch (CacheException e) {
                logger.error(strMethodName+e.getMessage());
                throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
            }catch (Exception e)
            {
                logger.error(strMethodName+e.getMessage());
                throw e;
            }
        }
        return nodeInfo;
    }

    /**
     * update an existing node's information
     *
     * @param nodeId node's id nodeInfo node's information, nodeId should be equal to nodeInfo's id
     * @return node's information
     * @throws ParameterNullOrEmptyException node inormation is input not valid, NodeRepositoryException exception caused by Repository
     */
    @Override
    @DurationStatistics
    public NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws ParameterNullOrEmptyException, NodeRepositoryException, Exception {
        String strMethodName = "updateNodeInfo";
        if (nodeId == null || nodeInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        else if (nodeId.equals(node.getId()) == false) {
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        }
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
                handleUpdateNodeRequest(nodeInfo);
            } catch (CacheException e) {
                logger.error(strMethodName+e.getMessage());
                throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
            }catch (Exception e)
            {
                logger.error(strMethodName+e.getMessage());
                throw e;
            }
        }
        return nodeInfo;
    }

    /**
     * delete an existing node's information
     *
     * @param nodeId node's id
     * @return node's id
     * @throws ParameterNullOrEmptyException node inormation is input not valid, NodeRepositoryException exception caused by Repository
     */
    @Override
    @DurationStatistics
    public String deleteNodeInfo(String nodeId) throws ParameterNullOrEmptyException, NodeRepositoryException, Exception {
        String strMethodName = "deleteNodeInfo";
        if (nodeId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        else if (nodeId.equals(node.getId()) == false)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        try {
            nodeRepository.deleteItem(nodeId);
            handleDeleteNodeRequest(nodeId);
        } catch (CacheException e) {
            logger.error(strMethodName+e.getMessage());
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }catch (Exception e)
        {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
        return nodeId;
    }
}
