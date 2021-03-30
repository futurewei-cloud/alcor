package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import com.futurewei.alcor.web.restclient.NetworkConfigManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(UpdateNodeInfoRequest.class);

    private NodeInfo nodeInfo;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfigManagerRestClient ncmRestClient;

    public UpdateNodeInfoRequest(NodeContext context, NodeInfo nodeInfo) {
        super(context);
        this.nodeInfo = nodeInfo;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.ncmRestClient = SpringContextUtil.getBean(NetworkConfigManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        NodeInfoJson jsonData = new NodeInfoJson(nodeInfo);
        dataPlaneManagerRestClient.updateNodeInfo(jsonData);
        ncmRestClient.updateNodeInfo(jsonData);
    }
}