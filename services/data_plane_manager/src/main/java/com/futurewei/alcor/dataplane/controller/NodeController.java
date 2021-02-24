package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.dataplane.exception.InvalidDataException;
import com.futurewei.alcor.dataplane.service.NodeService;
import com.futurewei.alcor.dataplane.utils.DatePlaneManagerConstant;
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
public class NodeController {
    private static final Logger LOG = LoggerFactory.getLogger();

    @Autowired
    NodeService nodeService;

    @PostMapping({"/nodes", "v4/nodes"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void createNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo inNodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            if(inNodeInfo != null) {
                if(inNodeInfo.validateIp(inNodeInfo.getLocalIp()) == false)
                    throw new InvalidDataException(DatePlaneManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
            }
            nodeService.createNodeInfo(nodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @PutMapping({"/nodes", "v4/nodes"})
    @DurationStatistics
    public void updateNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo inNodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            nodeService.updateNodeInfo(nodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }

    @DeleteMapping({"/nodes", "v4/nodes"})
    @DurationStatistics
    public void deleteNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        try {
            NodeInfo inNodeInfo = nodeInfoJson.getNodeInfo();
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(inNodeInfo);
            if(inNodeInfo != null) {
                if(inNodeInfo.validateIp(inNodeInfo.getLocalIp()) == false)
                    throw new InvalidDataException(DatePlaneManagerConstant.NODE_EXCEPTION_IP_FORMAT_INVALID);
            }
            nodeService.deleteNodeInfo(nodeInfoJson);
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
            throw new ParameterNullOrEmptyException(DatePlaneManagerConstant.NODE_EXCEPTION_JSON_EMPTY);
        }
        try {
            nodeService.createNodeInfoBulk(bulkNodeInfoJson);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
            throw e;
        }
    }
}
