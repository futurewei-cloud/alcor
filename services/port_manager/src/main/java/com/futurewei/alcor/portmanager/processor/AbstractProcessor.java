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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class AbstractProcessor implements IProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessor.class);

    private IProcessor nextProcessor;
    protected volatile PortConfigCache portConfigCache;
    protected NetworkConfig networkConfig;
    protected String projectId;
    protected PortRepository portRepository;
    protected List<CompletableFuture> futures = new ArrayList<>();

    @Override
    public IProcessor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public void setNextProcessor(IProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public PortConfigCache getPortConfigCache() {
        return portConfigCache;
    }

    @Override
    public void setPortConfigCache(PortConfigCache portConfigCache) {
        this.portConfigCache = portConfigCache;
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    @Override
    public void setNetworkConfig(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setPortRepository(PortRepository portRepository) {
        this.portRepository = portRepository;
    }

    abstract void createProcess(List<PortEntity> portEntities) throws Exception;

    abstract void updateProcess(String portId, PortEntity portEntity) throws Exception;

    private void execute(UpstreamRequest request, CallbackFunction callback) throws Exception {
        request.send();

        if (callback != null) {
            callback.apply(request);
        }
    }

    protected void sendRequest(UpstreamRequest request, CallbackFunction callback) {
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                execute(request, callback);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return null;
        }, AsyncExecutor.executor);

        futures.add(future);
    }

    private void initProcessor() {
        getNextProcessor().setProjectId(getProjectId());
        getNextProcessor().setNetworkConfig(getNetworkConfig());
        getNextProcessor().setPortConfigCache(getPortConfigCache());
    }

    @Override
    public void createPort(PortEntity portEntity) throws Exception {
        LOG.debug("createPort() processor: {}", this);

        createProcess(Arrays.asList(portEntity));
        if (getNextProcessor() != null) {
            initProcessor();
            getNextProcessor().createPort(portEntity);
        }
    }

    @Override
    public void createPortBulk(List<PortEntity> portEntities) throws Exception {
        LOG.debug("createPortBulk() processor: {}", this);

        createProcess(portEntities);
        if (getNextProcessor() != null) {
            initProcessor();
            getNextProcessor().createPortBulk(portEntities);
        }
    }

    @Override
    public void updatePort(String portId, PortEntity portEntity) throws Exception {
        updateProcess(portId, portEntity);
        if (getNextProcessor() != null) {
            initProcessor();
            getNextProcessor().updatePort(portId, portEntity);
        }
    }

    @Override
    public void waitProcessFinish() {
        futures.stream().forEach(CompletableFuture::join);
        if (getNextProcessor() != null) {
            getNextProcessor().waitProcessFinish();
        }
    }
}
