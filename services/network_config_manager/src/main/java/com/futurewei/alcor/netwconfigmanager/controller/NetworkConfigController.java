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

import java.util.List;
import java.util.logging.Level;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NetworkConfigController {

    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    private NodeService nodeService;

    @PostMapping({"/nodes", "v4/nodes"})
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

    @PostMapping({"/nodes/bulk","v4/nodes/bulk"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void createNodesInfoBulk(@RequestBody BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        if (bulkNodeInfoJson == null) {
            // Is it alright to lift this const from another package?
            // Perhaps, such constants belong the common.
            throw new ParameterNullOrEmptyException(NetworkConfigManagerConstants.NODE_EXCEPTION_JSON_EMPTY);
        }
        try {
            nodeService.createNodeInfoBulk(bulkNodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @PutMapping({"/nodes", "v4/nodes"})
    @DurationStatistics
    public void updateNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo nodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(nodeInfo);
            nodeService.updateNodeInfo(nodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @DeleteMapping({"/nodes", "v4/nodes"})
    @DurationStatistics
    public void deleteNodeInfo(@RequestBody String nodeId) throws Exception {
        try {
            nodeService.deleteNodeInfo(nodeId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }
}