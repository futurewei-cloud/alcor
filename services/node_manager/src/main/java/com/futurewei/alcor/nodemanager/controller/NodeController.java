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
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.entity.NodeInfoJson;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.RestPreconditionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class NodeController {

    @Autowired
    private NodeService service;

    @RequestMapping(
            method = GET,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    public NodeInfoJson getNodeInfoByMacAddress(@PathVariable String nodeid) throws Exception {
        NodeInfo hostInfo = null;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeid);
            hostInfo = service.getNodeInfoById(nodeid);
        } catch (ParameterNullOrEmptyException e) {
            //TODO: REST error code
            throw new Exception(e);
        }

        if (hostInfo == null) {
            //TODO: REST error code
            return new NodeInfoJson();
        }
        return new NodeInfoJson(hostInfo);
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
