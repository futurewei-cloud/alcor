package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(CreateNodeInfoRequest.class);

    private NodeInfo nodeInfo;

    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    public CreateNodeInfoRequest(NodeContext context, NodeInfo nodeInfo) {
        super(context);
        this.nodeInfo = nodeInfo;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        dataPlaneManagerRestClient.createNodeInfo(new NodeInfoJson(nodeInfo));
    }
}
