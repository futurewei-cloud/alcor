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

import com.futurewei.alcor.portmanager.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class ProcessorManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorManager.class);

    private static List<IProcessor> statelessProcessors = new ArrayList<>();
    private static Map<Class, IProcessor> allProcessors = new HashMap<>();
    private static IProcessor headProcessor;

    private void buildProcessChain() {
        IProcessor prev = headProcessor;
        for (IProcessor processor: statelessProcessors) {
            prev.setNextProcessor(processor);
            prev = processor;
        }
    }

    @PostConstruct
    private void init() throws Exception {
        Set<Class<? extends AbstractProcessor>> subClasses = ReflectionUtil.getSubClassByInterface(
                "com.futurewei.alcor.portmanager.processor", AbstractProcessor.class);

        for (Class<? extends IProcessor> subType: subClasses) {
            IProcessor processor = subType.getDeclaredConstructor().newInstance();
            if (processor instanceof PortProcessor) {
                headProcessor = processor;
            } else if (!(processor instanceof DatabaseProcessor) &&
                    !(processor instanceof DataPlaneProcessor)) {
                statelessProcessors.add(processor);
            }

            allProcessors.put(processor.getClass(), processor);
        }

        buildProcessChain();

        LOG.info("ProcessorManager init success");
    }

    public static IProcessor getProcessChain() {
        return headProcessor;
    }

    public static IProcessor getProcessor(Class tClass) {
        return allProcessors.get(tClass);
    }

    public static List<IProcessor> getStatelessProcessors() {
        return statelessProcessors;
    }
}
