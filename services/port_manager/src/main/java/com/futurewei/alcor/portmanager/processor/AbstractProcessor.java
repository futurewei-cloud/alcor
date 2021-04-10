/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
