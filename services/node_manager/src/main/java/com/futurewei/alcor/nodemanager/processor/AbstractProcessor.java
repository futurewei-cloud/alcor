/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.nodemanager.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessor implements IProcessor {
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