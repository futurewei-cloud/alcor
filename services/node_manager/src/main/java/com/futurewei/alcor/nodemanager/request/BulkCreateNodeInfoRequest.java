package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import com.futurewei.alcor.web.restclient.NetworkConfigManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BulkCreateNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(BulkCreateNodeInfoRequest.class);

    private List<NodeInfo> nodeInfos;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfigManagerRestClient ncmRestClient;

    public BulkCreateNodeInfoRequest(NodeContext context, List<NodeInfo> nodeInfos) {
        super(context);
        this.nodeInfos = nodeInfos;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.ncmRestClient = SpringContextUtil.getBean(NetworkConfigManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        BulkNodeInfoJson jsonData = new BulkNodeInfoJson(nodeInfos);
        dataPlaneManagerRestClient.bulkCreatNodeInfo(jsonData);
        /**
         * TEMP: After NcmInfo is integrated into NMM, NCM client will be located
         * by the NCM URI corresponding to this nodeId.
         */
        ncmRestClient.bulkCreatNodeInfo(jsonData);
    }
}
