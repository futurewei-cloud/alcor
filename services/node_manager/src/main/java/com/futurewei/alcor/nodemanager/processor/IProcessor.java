package com.futurewei.alcor.nodemanager.processor;

public interface IProcessor {
    void createNode(NodeContext context) throws Exception;
    void createNodeBulk(NodeContext context) throws Exception;
    void updateNode(NodeContext context) throws Exception;
    void deleteNode(NodeContext context) throws Exception;
    void setNextProcessor(IProcessor nextProcessor);
}
