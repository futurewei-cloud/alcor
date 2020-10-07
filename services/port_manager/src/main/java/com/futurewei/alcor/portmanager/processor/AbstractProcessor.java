/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessor implements IProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessor.class);

    private IProcessor nextProcessor;

    public IProcessor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public void setNextProcessor(IProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    abstract void createProcess(PortContext context) throws Exception;

    abstract void updateProcess(PortContext context) throws Exception;

    abstract void deleteProcess(PortContext context) throws Exception;

    @Override
    public void createPort(PortContext context) throws Exception {
        LOG.info("createPort() processor: {}", this);

        createProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().createPort(context);
        }
    }

    @Override
    public void createPortBulk(PortContext context) throws Exception {
        LOG.debug("createPortBulk() processor: {}", this);

        createProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().createPortBulk(context);
        }
    }

    @Override
    public void updatePort(PortContext context) throws Exception {
        LOG.debug("updatePort() processor: {}", this);

        updateProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().updatePort(context);
        }
    }

    @Override
    public void deletePort(PortContext context) throws Exception {
        LOG.debug("deletePort() processor: {}", this);

        deleteProcess(context);
        if (getNextProcessor() != null) {
            getNextProcessor().deletePort(context);
        }
    }
}
