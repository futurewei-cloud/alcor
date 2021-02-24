package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BulkCreateNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(BulkCreateNodeInfoRequest.class);

    private List<NodeInfo> nodeInfos;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    public BulkCreateNodeInfoRequest(NodeContext context, List<NodeInfo> nodeInfos) {
        super(context);
        this.nodeInfos = nodeInfos;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        dataPlaneManagerRestClient.bulkCreatNodeInfo(new BulkNodeInfoJson(nodeInfos));
    }
}
