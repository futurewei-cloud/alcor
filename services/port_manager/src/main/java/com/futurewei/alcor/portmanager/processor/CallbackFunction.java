package com.futurewei.alcor.portmanager.processor;


import com.futurewei.alcor.portmanager.request.IRestRequest;

@FunctionalInterface
public interface CallbackFunction {
    void apply(IRestRequest request) throws Exception;
}