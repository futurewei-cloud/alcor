package com.futurewei.alcor.nodemanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Stack;

public class DataPlaneManagerProxy {

    
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    // TODO: whether there should be a rollback
    // private Stack<Rollback> rollbacks;

    public DataPlaneManagerProxy() {
        dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
    }


    public NodeInfoJson creatNodeInfo(Object arg) throws Exception {
        NodeInfoJson nodeInfoJson = (NodeInfoJson)arg;
        dataPlaneManagerRestClient.createNodeInfo(nodeInfoJson);
        return nodeInfoJson;
    }
}
