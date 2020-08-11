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

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NodeController {

    @Autowired
    private NodeService service;

    @Autowired
    private HttpServletRequest request;

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
            method = GET,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public NodeInfoJson getNodeInfoById(@PathVariable String nodeid) throws Exception {
        NodeInfo hostInfo = null;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeid);
            hostInfo = service.getNodeInfoById(nodeid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        if (hostInfo == null) {
            return new NodeInfoJson();
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
                                    @ApiParam(value = "mac_address") @RequestParam(required = false) String mac_address,
                                    @ApiParam(value = "local_Ip") @RequestParam(required = false) String local_ip) throws ParameterNullOrEmptyException, Exception {
        List<NodeInfo> nodes = null;
        try {
            Map<String, Object[]> queryParams =
                    ControllerUtil.transformUrlPathParams(request.getParameterMap(), NodeInfo.class);
            if (name != null) {
                queryParams.put("name", new String[]{name});
            }
            if (id != null) {
                queryParams.put("id", new String[]{id});
            }
            if (mac_address != null) {
                queryParams.put("macAddress", new String[]{mac_address});
            }
            if (local_ip != null) {
                queryParams.put("localIp", new String[]{local_ip});
            }

            nodes = service.getAllNodes(queryParams);
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }
        if (nodes == null) {
            return new NodesWebJson();
        }
        return new NodesWebJson(nodes);
    }

    @RequestMapping(
            method = POST,
            value = {"/nodes", "/v4/nodes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public NodeInfoJson createNodeInfo(@RequestBody NodeInfoJson resource) throws ParameterNullOrEmptyException, InvalidDataException, ResourcePersistenceException, Exception  {
        NodeInfo hostInfo = null;
        try {
            NodeInfo inNodeInfo = resource.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            if(inNodeInfo != null) {
                if(inNodeInfo.validateIp(inNodeInfo.getLocalIp()) == false)
                    throw new InvalidDataException(NodeManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
            }
            hostInfo = service.createNodeInfo(inNodeInfo);
            if (hostInfo == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch(InvalidDataException e){
            throw e;
        }catch (Exception e) {
            throw e;
        }
        return new NodeInfoJson(hostInfo);
    }

    @RequestMapping(
            method = PUT,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public NodeInfoJson updateNodeInfo(@PathVariable String nodeid, @RequestBody NodeInfoJson resource) throws Exception {
        NodeInfo hostInfo = null;
        try {
            NodeInfo inNodeInfo = resource.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            RestPreconditionsUtil.verifyParameterValid(nodeid, inNodeInfo.getId());
            hostInfo = service.updateNodeInfo(nodeid, inNodeInfo);
            if (hostInfo == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        } catch (ParameterUnexpectedValueException e){
            throw e;
        }
        catch (Exception e) {
            throw new Exception(e);
        }
        return new NodeInfoJson(hostInfo);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public String deleteNodeInfo(@PathVariable String nodeid) throws Exception {
        String macAddress = null;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeid);
            macAddress = service.deleteNodeInfo(nodeid);
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }
        return "{Node(Node) Id: " + nodeid + "}";
    }
}
