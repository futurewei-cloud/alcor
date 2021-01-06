package com.futurewei.alcor.nodemanager.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessor implements IProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessor.class);

    private IProcessor nextProcessor;

    abstract void createProcess(NodeContext context) throws Exception;

    abstract void updateProcess(NodeContext context) throws Exception;

    abstract void deleteProcess(NodeContext context) throws Exception;

    abstract void bulkCreateProcess(NodeContext context) throws Exception;

    public IProcessor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public void setNextProcessor(IProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    @Override
    public void createNode(NodeContext context) throws Exception {
        LOG.info("createNode() processor: {}", this);

        createProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().createNode(context);
        }
    }

    @Override
    public void createNodeBulk(NodeContext context) throws Exception {
        LOG.debug("createNodeBulk() processor: {}", this);

        bulkCreateProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().createNodeBulk(context);
        }
    }

    @Override
    public void updateNode(NodeContext context) throws Exception {
        LOG.debug("updateNode() processor: {}", this);

        updateProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().updateNode(context);
        }
    }

    @Override
    public void deleteNode(NodeContext context) throws Exception {
        LOG.debug("deleteNode() processor: {}", this);

        deleteProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().deleteNode(context);
        }
    }
}
