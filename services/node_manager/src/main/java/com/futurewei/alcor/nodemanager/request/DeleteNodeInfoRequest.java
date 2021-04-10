package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import com.futurewei.alcor.web.restclient.NetworkConfigManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetPermission;

public class DeleteNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(DeleteNodeInfoRequest.class);

    private String nodeId;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfigManagerRestClient ncmRestClient;

    public DeleteNodeInfoRequest(NodeContext context, String nodeId) {
        super(context);
        this.nodeId = nodeId;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.ncmRestClient = SpringContextUtil.getBean(NetworkConfigManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        dataPlaneManagerRestClient.deleteNodeInfo(nodeId);
        ncmRestClient.deleteNodeInfo(nodeId);
    }
}