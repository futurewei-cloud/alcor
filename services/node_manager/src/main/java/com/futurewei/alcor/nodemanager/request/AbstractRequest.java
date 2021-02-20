package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.nodemanager.processor.NodeContext;

public abstract class AbstractRequest implements IRestRequest{
    protected NodeContext context;

    public AbstractRequest(NodeContext context) {
        this.context = context;
    }

    @Override
    public NodeContext getContext() {
        return context;
    }
}
