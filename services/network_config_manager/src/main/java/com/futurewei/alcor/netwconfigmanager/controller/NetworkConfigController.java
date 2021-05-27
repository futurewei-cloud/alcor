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
package com.futurewei.alcor.netwconfigmanager.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.netwconfigmanager.service.NodeService;
import com.futurewei.alcor.netwconfigmanager.util.Constants;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import java.util.List;
import java.util.logging.Level;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
public class NetworkConfigController {
    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    private NodeService nodeService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping({"/nodes", "/v4/nodes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void createNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo nodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeInfo);
            nodeService.createNodeInfo(nodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @PostMapping({"/nodes/bulk", "/v4/nodes/bulk"})
    @DurationStatistics
    public void createNodesInfoBulk(@RequestBody BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        if (bulkNodeInfoJson == null) {
            // Perhaps, this belongs in the common.
            throw new ParameterNullOrEmptyException(Constants.NODE_EXCEPTION_JSON_EMPTY);
        }
        try {
            nodeService.createNodeInfoBulk(bulkNodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @PutMapping({"/nodes", "/v4/nodes"})
    @DurationStatistics
    public void updateNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo nodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeInfo);
            nodeService.updateNodeInfo(nodeInfo);
        } catch (ParameterNullOrEmptyException e) {
            throw e;
        }
        catch (Exception e) {
            throw new Exception(e);
        }
    }

    @DeleteMapping({"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public void deleteNodeInfo(@PathVariable String nodeid) throws Exception {
        try {
            nodeService.deleteNodeInfo(nodeid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @GetMapping({"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public NodeInfoJson getNodeInfoById(@PathVariable String nodeid) throws Exception {
        NodeInfo hostInfo = null;
        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeid);
            hostInfo = nodeService.getNodeInfo(nodeid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }
        if (hostInfo == null) {
            return new NodeInfoJson();
        }
        return new NodeInfoJson(hostInfo);
    }
}
