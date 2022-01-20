package com.futurewei.alcor.portmanager.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractProcessorChainManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessorChainManager.class);

    Set<Class<?>> classes = new HashSet<>();
    public void buildProcessChain(ProcessorManager processorManager) {
        if (this.getClass().isAnnotationPresent(AfterProcessor.class)) {
            Class<?>[] processor = this.getClass().getAnnotation(AfterProcessor.class).exclude();
            classes = new HashSet<Class<?>>(Arrays.asList(processor));
        }
        LOG.info("Build process chain: ");

        processorManager.buildProcessChain(classes);

    }
}
