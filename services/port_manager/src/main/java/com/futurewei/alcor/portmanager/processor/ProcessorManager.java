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

import com.futurewei.alcor.portmanager.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class ProcessorManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorManager.class);

    private static List<IProcessor> processors = new ArrayList<>();
    private static Map<Class, IProcessor> processorMap = new HashMap<>();

    private void buildProcessChain() {
        LOG.info("Build process chain: ");

        if (processors.isEmpty()) {
            LOG.warn("Processor number is 0");
            return;
        }

        if (processors.size() == 1) {
            LOG.info("{}", processors.get(0));
            processors.get(0).setNextProcessor(null);
            return;
        }

        LOG.info("{}", processors.get(0));
        IProcessor prev = processors.get(0);

        for (int i = 1; i < processors.size(); i++) {
            prev.setNextProcessor(processors.get(i));
            prev = processors.get(i);
            LOG.info("{}", processors.get(i));
        }

        prev.setNextProcessor(null);
    }

    private void instanceProcessor(Class<? extends IProcessor> processorClass) throws Exception {
        if (processorClass.isAnnotationPresent(AfterProcessor.class)) {
            Class<?>[] parents = processorClass.getAnnotation(AfterProcessor.class).value();
            for (Class<?> parent: parents) {
                instanceProcessor((Class<? extends IProcessor>)parent);
            }
        }

        if (!processorMap.containsKey(processorClass)) {
            IProcessor processor = processorClass.getDeclaredConstructor().newInstance();
            processors.add(processor);
            processorMap.put(processorClass, processor);
        }
    }

    @PostConstruct
    private void init() throws Exception {
        Set<Class<? extends AbstractProcessor>> subClasses = ReflectionUtil.getSubClassByInterface(
                "com.futurewei.alcor.portmanager.processor", AbstractProcessor.class);

        for (Class<? extends IProcessor> subClass: subClasses) {
            instanceProcessor(subClass);
        }

        buildProcessChain();

        LOG.info("ProcessorManager init success");
    }

    public static IProcessor getProcessChain() {
        return processors.isEmpty() ? null : processors.get(0);
    }

    public static IProcessor getProcessor(Class tClass) {
        return processorMap.get(tClass);
    }

    public static List<IProcessor> getProcessors() {
        return processors;
    }
}
