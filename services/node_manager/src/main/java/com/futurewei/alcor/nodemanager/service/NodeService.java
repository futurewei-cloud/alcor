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
package com.futurewei.alcor.nodemanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.nodemanager.exception.UpdateNonExistingNodeException;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.json.simple.parser.ParseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NodeService {
    /**
     * read bulk nodes' information from file
     *
     * @param file nodes' file, e.g., machine.json
     * @return total number of nodes that got uploaded
     * @throws FileNotFoundException file not found exception
     * @throws IOException file read exception
     * @throws ParseException exception thrown for file parsing
     * @throws CacheException exception caused by Repository
     */
    int getNodeInfoFromUpload(MultipartFile file) throws FileNotFoundException, IOException, ParseException, CacheException;

    /**
     * get a node's information from repository
     *
     * @param nodeId: node's id
     * @return node's information
     * @throws CacheException exception caused by Repository
     */
    NodeInfo getNodeInfoById(String nodeId) throws CacheException;

    /**
     * get all nodes info filtered by params
     *
     * @param queryParams
     * @return List<NodeInfo>
     * @throws CacheException exception caused by Repository
     */
    List<NodeInfo> getAllNodes(Map<String, Object[]> queryParams) throws CacheException;

    /**
     * create a new node information
     *
     * @param nodeInfo new node's information
     * @return NodeInfo
     * @throws CacheException exception caused by Repository
     */
    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws CacheException;

    /**
     * create new nodes information in bulk
     *
     * @param nodeInfo new node's information in bulk
     * @throws CacheException exception caused by Repository
     */
    void createNodeInfoBulk(List<NodeInfo> nodeInfo) throws CacheException;

    /**
     * update an existing node's information
     *
     * @param nodeId node's id nodeInfo node's information, nodeId should be equal to nodeInfo's id
     * @throws CacheException exception caused by Repository
     * @throws UpdateNonExistingNodeException exception thrown when node to be updated doesn't exist
     */
    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws CacheException, UpdateNonExistingNodeException;

    /**
     * delete an existing node's information
     *
     * @param nodeId node's id
     * @throws CacheException exception caused by Repository
     * @throws UpdateNonExistingNodeException exception thrown when node to be deleted doesn't exist
     */
    void deleteNodeInfo(String nodeId) throws CacheException, UpdateNonExistingNodeException;
}
