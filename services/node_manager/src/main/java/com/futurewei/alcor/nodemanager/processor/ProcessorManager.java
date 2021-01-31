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
package com.futurewei.alcor.nodemanager.processor;

import com.futurewei.alcor.nodemanager.util.ReflectionUtil;
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
                "com.futurewei.alcor.nodemanager.processor", AbstractProcessor.class);

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
