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
package com.futurewei.alcor.portmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.enumClass.NetworkStatusEnum;
import com.futurewei.alcor.common.enumClass.StatusEnum;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.processor.*;
import com.futurewei.alcor.portmanager.repo.IPortRepository;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.request.CreateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.RouterUpdateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
public class PortServiceImpl implements PortService {
    private static final Logger LOG = LoggerFactory.getLogger(PortServiceImpl.class);

    @Autowired
    private IPortRepository portRepository;

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

    private Map<String, List<NeighborEntry>> buildNeighborTable(List<NeighborInfo> localNeighborInfos, List<NeighborInfo> l3Neighbors) {
        Map<String, List<NeighborEntry>> neighborTable = new HashMap<>();
        for (NeighborInfo localNeighborInfo: localNeighborInfos) {
            List<NeighborEntry> neighborEntries = new ArrayList<>();
            for (NeighborInfo l3Neighbor: l3Neighbors) {
                NeighborEntry neighborEntry = new NeighborEntry();
                neighborEntry.setNeighborType(NeighborEntry.NeighborType.L3);
                neighborEntry.setLocalIp(localNeighborInfo.getPortIp());
                neighborEntry.setNeighborIp(l3Neighbor.getPortIp());
                neighborEntries.add(neighborEntry);
            }

            String portIp = localNeighborInfo.getPortIp();
            neighborTable.put(portIp, neighborEntries);
        }

        return neighborTable;
    }

    private Map<String, List<NeighborEntry>> updateNeighborTable(RouterUpdateInfo routerUpdateInfo, Map<String, NeighborInfo> neighborInfos) throws CacheException {
        String vpcId = routerUpdateInfo.getVpcId();
        String subnetId = routerUpdateInfo.getSubnetId();
        List<String> gatewayPortIds = routerUpdateInfo.getGatewayPortIds();
        List<String> gatewaySubnetIds = new ArrayList<>();

        Map<String, Object[]> filterParams = new HashMap<>();
        filterParams.put("id", gatewayPortIds.toArray());
        Map<String, PortEntity> gatewayPortEntities = portRepository.findAllPortEntities(filterParams);
        if (gatewayPortEntities != null) {
            for (Map.Entry<String, PortEntity> entry: gatewayPortEntities.entrySet()) {
                PortEntity portEntity = entry.getValue();
                List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
                if (fixedIps == null) {
                    LOG.warn("Can not find fixedIp of gateway port: {}", portEntity);
                    continue;
                }

                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    String subnetId1 = fixedIp.getSubnetId();
                    if (!StringUtils.isEmpty(subnetId1)) {
                        gatewaySubnetIds.add(subnetId1);
                    }
                }
            }
        }

        Map<String, NeighborInfo> neighbors = portRepository.getNeighbors(vpcId);

        List<NeighborInfo> localNeighborInfos = new ArrayList<>();
        List<NeighborInfo> l3Neighbors = new ArrayList<>();

        for (Map.Entry<String, NeighborInfo> entry: neighbors.entrySet()) {
            NeighborInfo neighborInfo = entry.getValue();
            if (subnetId.equals(neighborInfo.getSubnetId())) {
                localNeighborInfos.add(neighborInfo);
                neighborInfos.put(neighborInfo.getPortIp(), neighborInfo);
            }else if (gatewaySubnetIds.contains(neighborInfo.getSubnetId())) {
                l3Neighbors.add(neighborInfo);
                neighborInfos.put(neighborInfo.getPortIp(), neighborInfo);
            }
        }

        return buildNeighborTable(localNeighborInfos, l3Neighbors);
    }

    @Override
    @DurationStatistics
    public RouterUpdateInfo updateL3Neighbors(String projectId, RouterUpdateInfo routerUpdateInfo) throws Exception {
        Map<String, NeighborInfo> neighborInfos = new HashMap<>();
        Map<String, List<NeighborEntry>> neighborTable = updateNeighborTable(routerUpdateInfo, neighborInfos);
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();
        internalRouterInfos.add(routerUpdateInfo.getInternalRouterInfo());

        for (String privateIp: neighborTable.keySet()) {
            LOG.info("NeighborTable- key: {}, size: {}", privateIp, neighborTable.get(privateIp).size());
        }
        if (neighborTable.size() > 0) {
            NetworkConfiguration networkConfiguration = new NetworkConfiguration();
            networkConfiguration.setRsType(Common.ResourceType.NEIGHBOR);
            String operationType = routerUpdateInfo.getOperationType();
            if (RouterUpdateInfo.OperationType.ADD.getType().equals(operationType)) {
                networkConfiguration.setOpType(Common.OperationType.CREATE);
            } else {
                networkConfiguration.setOpType(Common.OperationType.DELETE);
            }
            networkConfiguration.setNeighborInfos(neighborInfos);
            networkConfiguration.setNeighborTable(neighborTable);
            networkConfiguration.setInternalRouterInfos(internalRouterInfos);
            new UpdateNetworkConfigRequest(null, networkConfiguration).send();
        }

        return routerUpdateInfo;
    }

    @Override
    public int getSubnetPortCount(String projectId, String subnetId) throws Exception {
        return portRepository.getSubnetPortCount(subnetId);
    }

    @DurationStatistics
    @Override
    public void updatePortStatus(IRestRequest request,NetworkConfiguration configuration,String status) throws Exception {
        List<InternalPortEntity> internalPortEntities = configuration.getPortEntities();
        List<PortEntity> portEntities = new ArrayList<>();
        String name = request.getClass().getName();
        InternalDPMResultList result = null;
        if (CreateNetworkConfigRequest.class.getName().equals(name)) {
            CreateNetworkConfigRequest createRequest = (CreateNetworkConfigRequest)request;
            result = createRequest.getResultList();
        }
        if (UpdateNetworkConfigRequest.class.getName().equals(name)) {
            UpdateNetworkConfigRequest updateRequest = (UpdateNetworkConfigRequest)request;
            result = updateRequest.getResultList();
        }
        if(result == null){
            return;
        }
        for (InternalDPMResult internalDPMResult : result.getResultList()) {
            List<String> failedHosts = internalDPMResult.getFailedHosts();
            for (InternalPortEntity internalPortEntity : internalPortEntities) {
                PortEntity portEntity = portRepository.findPortEntity(internalPortEntity.getId());
                if (status != null) {
                    portEntity.setStatus(status);
                }else if (failedHosts.contains(internalPortEntity.getBindingHostIP())){
                    portEntity.setStatus(StatusEnum.FAILED.getStatus());
                }else {
                    portEntity.setStatus(NetworkStatusEnum.ACTIVE.getNetworkStatus());
                }
                portEntities.add(portEntity);
            }
        }
        portRepository.addPortEntities(portEntities);
    }
}
