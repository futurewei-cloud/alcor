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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.processor.*;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.router.RouterSubnetUpdateInfo;
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
    @DurationStatistics
    public PortWebJson createPort(String projectId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Create port enter, projectId: {}, PortWebJson: {}", projectId, portWebJson);

        createPortEntities(projectId, Collections.singletonList(portWebJson.getPortEntity()));

        LOG.info("Create port, projectId: {}, PortWebJson: {}", projectId, portWebJson);

        return portWebJson;
    }

    @Override
    @DurationStatistics
    public PortWebBulkJson createPortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        LOG.debug("Create port bulk enter, projectId: {}, PortWebBulkJson: {}", projectId, portWebBulkJson);

        createPortEntities(projectId, portWebBulkJson.getPortEntities());

        LOG.info("Create port bulk success, projectId: {}, PortWebBulkJson: {}", projectId, portWebBulkJson);

        return portWebBulkJson;
    }

    @Override
    @DurationStatistics
    public PortWebJson updatePort(String projectId, String portId, PortWebJson portWebJson) throws Exception {
        LOG.debug("Update port enter, projectId: {}, portId: {}, PortWebJson: {}",
                projectId, portId, portWebJson);

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

        LOG.info("Update port success, projectId: {}, portId: {}, PortWebJson: {}",
                projectId, portId, portWebJson);

        return portWebJson;
    }

    @Override
    public PortWebBulkJson updatePortBulk(String projectId, PortWebBulkJson portWebBulkJson) throws Exception {
        return null;
    }

    @Override
    @DurationStatistics
    public void deletePort(String projectId, String portId) throws Exception {
        LOG.debug("Delete port enter, projectId: {}, portId: {}", projectId, portId);

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

        LOG.info("Delete port success, projectId: {}, portId: {}", projectId, portId);
    }

    @Override
    @DurationStatistics
    public PortWebJson getPort(String projectId, String portId) throws Exception {
        PortEntity portEntity = portRepository.findPortEntity(portId);
        if (portEntity == null) {
            throw new PortEntityNotFound();
        }

        LOG.info("Get port success, projectId: {}, portId: {}", projectId, portId);

        return new PortWebJson(portEntity);
    }

    @Override
    @DurationStatistics
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

        LOG.info("List port success, projectId: {}", projectId);

        return result;
    }

    @Override
    @DurationStatistics
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

        LOG.info("List port success, projectId: {}", projectId);

        return result;
    }

    private List<NeighborInfo> doUpdateNeighbors(RouterSubnetUpdateInfo routerSubnetUpdateInfo) throws CacheException {
        String vpcId = routerSubnetUpdateInfo.getVpcId();
        String subnetId = routerSubnetUpdateInfo.getSubnetId();
        List<String> oldSubnetIds = routerSubnetUpdateInfo.getOldSubnetIds();
        RouterSubnetUpdateInfo.OperationType operationType = routerSubnetUpdateInfo.getOperationType();

        Map<String, NeighborInfo> neighbors = portRepository.getNeighbors(vpcId);

        for (Map.Entry<String, NeighborInfo> entry: neighbors.entrySet()) {
            NeighborInfo neighborInfo = entry.getValue();
            if (subnetId.equals(neighborInfo.getSubnetId())) {
                if (operationType.equals(RouterSubnetUpdateInfo.OperationType.ADD)) {
                    neighborInfo.setNeighborType(NeighborInfo.NeighborType.L3);
                } else {
                    neighborInfo.setNeighborType(NeighborInfo.NeighborType.L2);
                }
            } else if (oldSubnetIds.contains(neighborInfo.getSubnetId())) {
                neighborInfo.setNeighborType(NeighborInfo.NeighborType.L3);
            } else {
                neighborInfo.setNeighborType(NeighborInfo.NeighborType.L2);
            }
        }

        return new ArrayList<>(neighbors.values());
    }

    @Override
    @DurationStatistics
    public RouterSubnetUpdateInfo updateNeighbors(String projectId, RouterSubnetUpdateInfo routerSubnetUpdateInfo) throws Exception {
        List<NeighborInfo> neighbors = doUpdateNeighbors(routerSubnetUpdateInfo);

        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setRsType(Common.ResourceType.NEIGHBOR);
        networkConfiguration.setOpType(Common.OperationType.NEIGHBOR_CREATE_UPDATE);
        networkConfiguration.setNeighborInfos(neighbors);

        new UpdateNetworkConfigRequest(null, networkConfiguration).send();

        return routerSubnetUpdateInfo;
    }
}
