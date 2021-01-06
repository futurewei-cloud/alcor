package com.futurewei.alcor.nodemanager.processor;

import com.futurewei.alcor.nodemanager.request.CreateNodeInfoRequest;
import com.futurewei.alcor.nodemanager.request.DeleteNodeInfoRequest;
import com.futurewei.alcor.nodemanager.request.IRestRequest;
import com.futurewei.alcor.nodemanager.request.UpdateNodeInfoRequest;

public class NodeProcessor extends AbstractProcessor{
    @Override
    void createProcess(NodeContext context) throws Exception {
        IRestRequest createNodeRequest = new CreateNodeInfoRequest(context, context.getNodeInfo());
        context.getRequestManager().sendRequestAsync(createNodeRequest);
    }

    @Override
    void updateProcess(NodeContext context) throws Exception {
        IRestRequest updateNodeRequest = new UpdateNodeInfoRequest(context, context.getNodeInfo());
        context.getRequestManager().sendRequestAsync(updateNodeRequest);
    }

    @Override
    void deleteProcess(NodeContext context) throws Exception {
        IRestRequest deleteNodeRequest = new DeleteNodeInfoRequest(context, context.getNodeInfo().getId());
        context.getRequestManager().sendRequestAsync(deleteNodeRequest);
    }
}
