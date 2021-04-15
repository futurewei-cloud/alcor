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
package com.futurewei.alcor.nodemanager.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.nodemanager.dao.NcmInfoRepository;
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.exception.NodeRepositoryException;
import com.futurewei.alcor.nodemanager.processor.IProcessor;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.nodemanager.processor.ProcessorManager;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.web.entity.node.NcmInfo;
import com.futurewei.alcor.web.entity.node.NcmInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private NcmInfoRepository ncmInfoRepository;

    private void handleCreateNodeRequest(NodeInfo nodeInfo) {
        NodeContext nodeContext = new NodeContext(nodeInfo);
        IProcessor processorChain = ProcessorManager.getProcessChain();

        try {
                processorChain.createNode(nodeContext);
                nodeContext.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            logger.error("Catch exception: ", e);
        }
    }

    private void handleUpdateNodeRequest(NodeInfo nodeInfo) {
        NodeContext nodeContext = new NodeContext(nodeInfo);
        IProcessor processorChain = ProcessorManager.getProcessChain();
        try {
            processorChain.updateNode(nodeContext);
            nodeContext.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            logger.error("Catch exception: ", e);
        }
    }

    private void handleDeleteNodeRequest(String nodeId) {
        NodeContext nodeContext = new NodeContext(nodeId);
        IProcessor processorChain = ProcessorManager.getProcessChain();
        try {
            processorChain.deleteNode(nodeContext);
            nodeContext.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            logger.error("Catch exception: ", e);
        }
    }

    private void handleCreateNodeBulkRequest(List<NodeInfo> nodeInfos) {
        NodeContext nodeContext = new NodeContext(nodeInfos);
        IProcessor processorChain = ProcessorManager.getProcessChain();
        try {
            processorChain.createNodeBulk(nodeContext);
            nodeContext.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            logger.error("Catch exception: ", e);
        }
    }

    private void augmentNodeInfosWithNcmUri(List<NodeInfo> nodeInfos) throws Exception {
        for (int i = 0; i < nodeInfos.size(); ++i) {
            NodeInfo ni = nodeInfos.get(i);
            addNcmUriToNodeInfo(ni);
        }
    }

    private void addNcmUriToNodeInfo(NodeInfo nodeInfo) throws Exception {
        NcmInfo ncmInfo = ncmInfoRepository.getNcmInfoById(nodeInfo.getNcmId());
        if (ncmInfo == null || ncmInfo.getUri() == null) {
            String except = NodeManagerConstant.NODE_EXCEPTION_NCM_NOT_FOUND + " ncmid = " + nodeInfo.getNcmId();
            logger.error(except);
            throw new Exception(except);
        }
        nodeInfo.setNcmUri(ncmInfo.getUri());
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
            augmentNodeInfosWithNcmUri(nodeList);
            nodeRepository.addItemBulkTransaction(nodeList);
            nReturn = nodeList.size();
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
        addNcmUriToNodeInfo(nodeInfo);
        NodeInfo node = getNodeInfoById(nodeInfo.getId());
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
                this.handleCreateNodeRequest(nodeInfo);
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
            augmentNodeInfosWithNcmUri(nodeInfo);
            try {
                nodeRepository.addItemBulkTransaction(nodeInfo);
                this.handleCreateNodeBulkRequest(nodeInfo);
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
                addNcmUriToNodeInfo(nodeInfo);
                nodeRepository.addItem(nodeInfo);
                this.handleUpdateNodeRequest(nodeInfo);
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
            addNcmUriToNodeInfo(node);
            nodeRepository.deleteItem(nodeId);
            this.handleDeleteNodeRequest(nodeId);
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


    /**
     * /ncms end-point handlers
     */
    @Override
    public void registerNcmMetaData(NcmInfo ncmInfo) throws Exception {
        String strMethodName = "registerNcmMetaData";
        if (ncmInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NcmInfo oldEntry = ncmInfoRepository.getNcmInfoById(ncmInfo.getId());
        if (oldEntry != null) {
            logger.error(strMethodName + NodeManagerConstant.NODE_EXCEPTION_ENTRY_EXIST);
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_ENTRY_EXIST);
        }
        addOrUpdateNcmData(ncmInfo, strMethodName);
    }

    @Override
    public void unRegisterNcmMetaData(String ncmId) throws Exception {
        String strMethodName = "unRegisterNcmMetaData";
        if (ncmId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NcmInfo oldEntry = ncmInfoRepository.getNcmInfoById(ncmId);
        if (oldEntry == null) {
            logger.error(strMethodName + NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
        }
        try {
            ncmInfoRepository.deleteNcmInfo(ncmId);
        } catch (CacheException e) {
            logger.error(strMethodName+e.getMessage());
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
    }

    @Override
    public NcmInfo getNcmMetaData(String ncmId) throws Exception {
        NcmInfo ncmInfo = null;

        String strMethodName = "getNcmMetaData";
        if (ncmId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        ncmInfo = ncmInfoRepository.getNcmInfoById(ncmId);
        if (ncmInfo == null) {
            logger.error(strMethodName + NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
        }
        return ncmInfo;
    }

    @Override
    public List<NcmInfo> getAllNcmMetaData() throws Exception {
        String strMethodName = "getAllNcmMetaData";
        Map<String, NcmInfo> ncmInfoMap = ncmInfoRepository.findAllNcmInfo();
        if (ncmInfoMap == null) {
            logger.error(strMethodName + NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EMPTY);
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EMPTY);
        }

        List<NcmInfo> ncmInfoList = new ArrayList<>();
        for (Map.Entry<String, NcmInfo> entry: ncmInfoMap.entrySet()) {
            ncmInfoList.add(new NcmInfo(entry.getValue()));
        }

        return ncmInfoList;
    }

    @Override
    public void updateNcmMetaData(String ncmId, NcmInfo ncmInfo) throws Exception {
        NcmInfo oldNcmInfo = null;

        String strMethodName = "getNcmMetaData";
        if (ncmId == null || ncmInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        oldNcmInfo = ncmInfoRepository.getNcmInfoById(ncmId);
        if (oldNcmInfo == null) {
            logger.error(strMethodName + NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_ENTRY_NOT_FOUND);
        }
        addOrUpdateNcmData(ncmInfo, strMethodName);
    }

    private void addOrUpdateNcmData(NcmInfo ncmInfo, String strMethodName) throws NodeRepositoryException {
        try {
            ncmInfoRepository.addNcmInfo(ncmInfo);
        } catch (CacheException e) {
            logger.error(strMethodName+e.getMessage());
            throw new NodeRepositoryException(NodeManagerConstant.NODE_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error(strMethodName+e.getMessage());
            throw e;
        }
    }
}