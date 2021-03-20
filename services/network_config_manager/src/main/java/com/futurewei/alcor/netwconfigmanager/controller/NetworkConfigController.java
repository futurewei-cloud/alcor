package com.futurewei.alcor.netwconfigmanager.controller;
 
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.netwconfigmanager.service.NodeService;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerConstants;
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
public class NetworkConfigController {

    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    private NodeService nodeService;
    @Autowired
    private HttpServletRequest request;
    @RequestMapping(
            method = POST,
            value = {"/nodes", "/v4/nodes"})
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

    @RequestMapping(
            method = POST,
            value = {"/nodes/bulk", "/v4/nodes/bulk"})
    public void createNodesInfoBulk(@RequestBody BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        if (bulkNodeInfoJson == null) {
            // Perhaps, this belongs in the common.
            throw new ParameterNullOrEmptyException(NetworkConfigManagerConstants.NODE_EXCEPTION_JSON_EMPTY);
        }
        try {
            nodeService.createNodeInfoBulk(bulkNodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @RequestMapping(
            method = PUT,
            value = {"/nodes", "/v4/nodes"})
    @DurationStatistics
    public void updateNodeInfo(String nodeId, @RequestBody NodeInfoJson nodeInfoJson) throws Exception {
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

    @RequestMapping(
            method = DELETE,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
    @DurationStatistics
    public void deleteNodeInfo(@PathVariable String nodeid) throws Exception {
        String macAddress = null;
        try {
            nodeService.deleteNodeInfo(nodeid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @RequestMapping(
            method = GET,
            value = {"/nodes/{nodeid}", "/v4/nodes/{nodeid}"})
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
