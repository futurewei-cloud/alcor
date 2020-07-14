package com.futurewei.alcor.portmanager.processor;


import com.futurewei.alcor.portmanager.request.UpstreamRequest;

@FunctionalInterface
public interface CallbackFunction {
    void apply(UpstreamRequest request) throws Exception;
}