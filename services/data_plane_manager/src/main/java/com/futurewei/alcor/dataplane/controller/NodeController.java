package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.service.NodeService;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class NodeController {
    @Autowired
    NodeService nodeService;

    @PostMapping({"/node-info", "v4/node-info"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void createNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        nodeService.createNodeInfo(nodeInfoJson);
    }

    @PutMapping({"/node-info", "v4/node-info"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void updateNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        nodeService.createNodeInfo(nodeInfoJson);
    }

    @DeleteMapping({"/node-info", "v4/node-info"})
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public void deleteNodeInfo(@RequestBody NodeInfoJson nodeInfoJson) throws Exception {
        nodeService.createNodeInfo(nodeInfoJson);
    }
}
