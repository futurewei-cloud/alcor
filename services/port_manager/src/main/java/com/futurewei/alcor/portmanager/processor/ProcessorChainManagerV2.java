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
package com.futurewei.alcor.portmanager.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AfterProcessor(value = {Object.class}, exclude = {NeighborProcessor.class})
@ConditionalOnProperty(prefix = "protobuf.goal-state-message", name = "version", havingValue = "102")
public class ProcessorChainManagerV2 implements IProcessorChainManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorManager.class);

    Set<Class<?>> classes = new HashSet<>();

    @Override
    public List<IProcessor> buildProcessChain(List<IProcessor> processors) {
        if (this.getClass().isAnnotationPresent(AfterProcessor.class)) {
            Class<?>[] processor = this.getClass().getAnnotation(AfterProcessor.class).exclude();
            classes = new HashSet<Class<?>>(Arrays.asList(processor));
        }
        LOG.info("Build process chain: ");

        if (processors.isEmpty()) {
            LOG.warn("Processor number is 0");
            return processors;
        }

        if (processors.size() == 1) {
            LOG.info("{}", processors.get(0));
            processors.get(0).setNextProcessor(null);
            return processors;
        }

        LOG.info("{}", processors.get(0));
        IProcessor prev = processors.get(0);

        for (int i = 1; i < processors.size(); i++) {
            if (classes.contains(processors.get(i).getClass())) {
                processors.remove(i);
                continue;
            }
            prev.setNextProcessor(processors.get(i));
            prev = processors.get(i);
            LOG.info("{}", processors.get(i));
        }

        prev.setNextProcessor(null);
        return processors;
    }
}
