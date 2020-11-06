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
package com.futurewei.alcor.nodemanager.controller;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.nodemanager.exception.UpdateNonExistingNodeException;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NodeController {
    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    private NodeService service;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(
            method = GET,
            value = {"/nodes/{nodeId}", "/v4/nodes/{nodeId}"})
    @DurationStatistics
    public NodeInfoJson getNodeInfoById(@PathVariable String nodeId) throws Exception {
        NodeInfo hostInfo;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeId);
            hostInfo = service.getNodeInfoById(nodeId);
        } catch (ParameterNullOrEmptyException e) {
            LOG.log(Level.INFO, e.getMessage());
            throw e;
        } catch (CacheException e) {
            LOG.log(Level.WARNING, e.getMessage());
            throw e;
        }

        return new NodeInfoJson(hostInfo);
    }

    @FieldFilter(type = NodeInfo.class)
    @RequestMapping(
            method = GET,
            value = {"/nodes", "/v4/nodes"})
    @DurationStatistics
    public NodesWebJson getAllNodes(@ApiParam(value = "node_name") @RequestParam(required = false) String name,
                                    @ApiParam(value = "node_id") @RequestParam(required = false) String id,
                                    @ApiParam(value = "mac_address") @RequestParam(required = false) String macAddress,
                                    @ApiParam(value = "local_Ip") @RequestParam(required = false) String localIp) throws Exception {
        List<NodeInfo> nodes;
        try {
            Map<String, Object[]> queryParams =
                    ControllerUtil.transformUrlPathParams(request.getParameterMap(), NodeInfo.class);
            if (name != null) {
                queryParams.put("name", new String[]{name});
            }
            if (id != null) {
                queryParams.put("id", new String[]{id});
            }
            if (macAddress != null) {
                queryParams.put("macAddress", new String[]{macAddress});
            }
            if (localIp != null) {
                queryParams.put("localIp", new String[]{localIp});
            }

            nodes = service.getAllNodes(queryParams);
        } catch (CacheException e) {
            LOG.log(Level.WARNING, e.getMessage());
            throw e;
        }

        return new NodesWebJson(nodes);
    }

    @RequestMapping(
            method = POST,
            value = {"/nodes", "/v4/nodes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void createNodeInfo(@RequestBody NodeInfoJson resource) throws Exception {

        try {
            NodeInfo inNodeInfo = resource.getNodeInfo();
            if (inNodeInfo == null)
                throw new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY);
            if (!inNodeInfo.validate())
                throw new InvalidDataException(NodeManagerConstant.NODE_CONTENT_INVALID_EXCEPTION + inNodeInfo.getId());

            service.createNodeInfo(inNodeInfo);
        } catch (ParameterNullOrEmptyException | InvalidDataException e) {
            LOG.log(Level.INFO, e.getMessage());
            throw e;
        } catch (CacheException e) {
            LOG.log(Level.WARNING, e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new UnsupportedOperationException(e);
        }
    }

    @RequestMapping(
            method = POST,
            value = {"/nodes/bulk", "/v4/nodes/bulk"})
    public void registerNodesInfoBulk(@RequestBody BulkNodeInfoJson resource) throws Exception {
        if (resource == null || resource.getNodeInfos() == null) {
            throw new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_JSON_EMPTY);
        }

        try {
            service.createNodeInfoBulk(resource.getNodeInfos());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    @RequestMapping(
            method = POST,
            value = {"/nodes/upload", "/v4/nodes/upload"})
    @DurationStatistics
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        int nNode = 0;
        if (file == null) {
            throw new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_FILE_EMPTY);
        }

        try {
            nNode = service.getNodeInfoFromUpload(file);
        } catch (Exception e) {
            throw e;
        }
        return "{Total nodes: " + nNode + "}";
    }

    @RequestMapping(
            method = PUT,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public NodeInfoJson updateNodeInfo(@PathVariable String nodeId, @RequestBody NodeInfoJson resource) throws Exception {
        NodeInfo hostInfo;
        try {
            NodeInfo inNodeInfo = resource.getNodeInfo();
            if (nodeId == null || inNodeInfo == null)
                throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
            RestPreconditionsUtil.verifyParameterValid(nodeId, inNodeInfo.getId());

            hostInfo = service.updateNodeInfo(nodeId, inNodeInfo);
        } catch (ParameterNullOrEmptyException | ParameterUnexpectedValueException e) {
            LOG.log(Level.INFO, e.getMessage());
            throw e;
        } catch (CacheException e) {
            LOG.log(Level.WARNING, e.getMessage());
            throw e;
        } catch (UpdateNonExistingNodeException e) {
            LOG.log(Level.INFO, e.getMessage() + "Node Id :" + nodeId);
            throw e;
        }

        return new NodeInfoJson(hostInfo);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/nodes/{nodeId}", "/v4/nodes/{nodeId}"})
    @DurationStatistics
    public String deleteNodeInfo(@PathVariable String nodeId) throws Exception {
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeId);
            service.deleteNodeInfo(nodeId);
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (CacheException e) {
            LOG.log(Level.WARNING, e.getMessage());
            throw e;
        } catch (UpdateNonExistingNodeException e) {
            LOG.log(Level.INFO, e.getMessage() + "Node Id :" + nodeId);
            throw e;
        }

        return "{Node Id: " + nodeId + "}";
    }
}
