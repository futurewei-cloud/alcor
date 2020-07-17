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
package com.futurewei.alcor.portmanager.service;

import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.processor.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private PortRepository portRepository;

    private void handleException(PortContext context, Exception e) throws Exception {
        LOG.error("Catch exception: ", e);
        context.getRequestManager().rollbackAllRequests();
        throw e;
    }

    private void createPortEntities(String projectId, List<PortEntity> portEntities) throws Exception {
        PortConfigCache portConfigCache = new PortConfigCache();
        PortEntityParser.parse(portEntities, portConfigCache);

        PortContext context = new PortContext(portConfigCache, projectId, portRepository);
        context.setPortEntities(portEntities);

        IProcessor processChain = ProcessorManager.getProcessChain();

        try {
            processChain.createPortBulk(context);
            context.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            handleException(context, e);
        }
    }

    @Override
    public PortWebJson createPort(String projectId, PortWebJson portWebJson) throws Exception {
        createPortEntities(projectId, Collections.singletonList(portWebJson.getPortEntity()));
        return portWebJson;
    }

    @Override
    public PortWebBulkJson createPortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        createPortEntities(projectId, portWebBulkJson.getPortEntities());
        return portWebBulkJson;
    }

    @Override
    public PortWebJson updatePort(String projectId, String portId, PortWebJson portWebJson) throws Exception {
        PortEntity portEntity = portRepository.findPortEntity(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        PortContext context = new PortContext(null, projectId, portRepository);
        context.setOldPortEntity(portEntity);
        context.setNewPortEntity(portWebJson.getPortEntity());

        IProcessor processChain = ProcessorManager.getProcessChain();

        try {
            processChain.updatePort(context);
            context.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            handleException(context, e);
        }

        portWebJson.setPortEntity(portEntity);
        return portWebJson;
    }

    @Override
    public PortWebBulkJson updatePortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        return null;
    }

    @Override
    public void deletePort(String projectId, String portId) throws Exception {
        PortEntity portEntity = portRepository.findPortEntity(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        PortContext context = new PortContext(null, projectId, portRepository);
        context.setPortEntities(Collections.singletonList(portEntity));

        IProcessor processChain = ProcessorManager.getProcessChain();

        try {
            processChain.deletePort(context);
            context.getRequestManager().waitAllRequestsFinish();
        } catch (Exception e) {
            handleException(context, e);
        }
    }

    @Override
    public PortWebJson getPort(String projectId, String portId) throws Exception {
        PortEntity portEntity = portRepository.findPortEntity(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        return new PortWebJson(portEntity);
    }

    @Override
    public List<PortWebJson> listPort(String projectId) throws Exception {
        List<PortWebJson> result = new ArrayList<>();

        Map<String, PortEntity> portEntityMap = portRepository.findAllPortEntities();
        if (portEntityMap == null) {
            return result;
        }

        for (Map.Entry<String, PortEntity> entry : portEntityMap.entrySet()) {
            PortWebJson portWebJson = new PortWebJson(entry.getValue());
            result.add(portWebJson);
        }

        return result;
    }

    @Override
    public List<PortWebJson> listPort(String projectId, Map<String, Object[]> queryParams) throws Exception {
        List<PortWebJson> result = new ArrayList<>();

        Map<String, PortEntity> portEntityMap = portRepository.findAllPortEntities(queryParams);
        if (portEntityMap == null) {
            return result;
        }

        for (Map.Entry<String, PortEntity> entry: portEntityMap.entrySet()) {
            PortWebJson portWebJson = new PortWebJson(entry.getValue());
            result.add(portWebJson);
        }

        return result;
    }
}
