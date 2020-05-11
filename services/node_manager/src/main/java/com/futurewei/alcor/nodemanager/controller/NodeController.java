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
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.entity.NodeInfoJson;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class NodeController {

    @Autowired
    private NodeService service;

    @RequestMapping(
            method = POST,
            value = {"/nodes/upload", "/v4/nodes/upload"})
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        int nNode = 0;
        if (file == null) {
            return NodeManagerConstant.NODE_EXCEPTION_FILE_EMPTY;
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
            value = {"/nodes/path/{path}/**", "/v4/nodes/path/{path}/**"})
    public String getNodeInfoFromFile(@PathVariable String path, HttpServletRequest request) throws Exception {
        int nNode = 0;
        String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String strPath = restOfTheUrl.substring(12);
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(strPath);
            nNode = service.getNodeInfoFromFile(strPath);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        return "{Total nodes: " + nNode + "}";
    }

    @RequestMapping(
            method = GET,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    public NodeInfoJson getNodeInfoByMacAddress(@PathVariable String nodeid) throws Exception {
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

    @RequestMapping(
            method = GET,
            value = {"/nodes", "/v4/nodes"})
    public List<NodeInfo> getAllNodes() throws Exception {
        List<NodeInfo> nodes = null;
        try {
            nodes = service.getAllNodes();
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        if (nodes == null) {
            return new ArrayList();
        }
        return nodes;
    }

    @RequestMapping(
            method = POST,
            value = {"/nodes", "/v4/nodes"})
    @ResponseStatus(HttpStatus.CREATED)
    public NodeInfoJson createNodeInfo(@RequestBody NodeInfoJson resource) throws Exception {
        NodeInfo hostInfo = null;
        try {
            NodeInfo inNodeInfo = resource.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            hostInfo = service.createNodeInfo(inNodeInfo);
            if (hostInfo == null) {
                throw new ResourcePersistenceException();
            }
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new NodeInfoJson(hostInfo);
    }

    @RequestMapping(
            method = PUT,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
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
            throw new Exception(e);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new NodeInfoJson(hostInfo);
    }

    @RequestMapping(
            method = DELETE,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    public String deleteMacAllocation(@PathVariable String nodeid) throws Exception {
        String macAddress = null;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeid);
            macAddress = service.deleteNodeInfo(nodeid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        return "{Node(Node) Id: " + nodeid + "}";
    }
}
