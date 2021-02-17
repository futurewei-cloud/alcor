package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.nodemanager.processor.NodeContext;

public interface IRestRequest {
    void send() throws Exception;
    NodeContext getContext();
}
