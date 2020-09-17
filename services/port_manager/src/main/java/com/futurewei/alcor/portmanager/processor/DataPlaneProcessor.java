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

import com.futurewei.alcor.portmanager.exception.GetNodeInfoException;
import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.request.CreateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.DeleteNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataPlaneProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneProcessor.class);

    private PortEntity getPortEntity(List<PortEntity> portEntities, String portId) {
        for (PortEntity portEntity: portEntities) {
            if (portEntity.getId().equals(portId)) {
                return portEntity;
            }
        }

        return null;
    }

    private List<NeighborEntry> buildNeighborTable(NeighborInfo localInfo, List<NeighborInfo> neighbors, NeighborEntry.NeighborType neighborType) {
        List<NeighborEntry> neighborTable = new ArrayList<>();
        for (NeighborInfo neighbor: neighbors) {
            NeighborEntry neighborEntry = new NeighborEntry();
            neighborEntry.setNeighborType(neighborType);
            neighborEntry.setLocalIp(localInfo.getPortIp());
            neighborEntry.setNeighborIp(neighbor.getPortId());
            neighborTable.add(neighborEntry);
        }

        return neighborTable;
    }

    private NeighborInfo getNeighborInfo(PortContext context, InternalPortEntity internalPortEntity, PortEntity.FixedIp fixedIp) throws Exception {
        List<NodeInfo> nodeInfos = context.getNodeInfos();
        NeighborInfo neighborInfo = null;
        for (NodeInfo nodeInfo: nodeInfos) {
            if (internalPortEntity.getBindingHostIP().equals(nodeInfo.getLocalIp())) {
                neighborInfo = new NeighborInfo();
                neighborInfo.setPortId(internalPortEntity.getId());
                neighborInfo.setPortIp(fixedIp.getIpAddress());
                neighborInfo.setPortMac(internalPortEntity.getMacAddress());
                neighborInfo.setHostId(nodeInfo.getId());
                neighborInfo.setHostIp(nodeInfo.getLocalIp());
                break;
            }
        }

        if (neighborInfo == null) {
            throw new GetNodeInfoException();
        }

        return neighborInfo;
    }

    private void buildL3Neighbors(PortContext context, InternalPortEntity internalPortEntity, PortEntity.FixedIp fixedIp, List<String> routerSubnetIds) throws Exception {
        List<NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();

        List<NeighborInfo> l3Neighbors = new ArrayList<>();
        NeighborInfo localNeighborInfo = null;
        for (NeighborInfo neighborInfo: neighborInfos) {
            if (neighborInfo.getPortId().equals(fixedIp.getIpAddress())) {
                localNeighborInfo = neighborInfo;
            }else if (routerSubnetIds.contains(neighborInfo.getSubnetId()) &&
                    !neighborInfo.getSubnetId().equals(fixedIp.getSubnetId())) {
                l3Neighbors.add(neighborInfo);
            }
        }

        if (localNeighborInfo == null) {
            localNeighborInfo = getNeighborInfo(context, internalPortEntity, fixedIp);
        }

        List<NeighborEntry> neighborTable = buildNeighborTable(
                localNeighborInfo, l3Neighbors, NeighborEntry.NeighborType.L3);
        context.getNetworkConfig().addNeighborEntries(neighborTable);
    }

    private void buildL2Neighbors(PortContext context, InternalPortEntity internalPortEntity, PortEntity.FixedIp fixedIp) throws Exception {
        List<NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();

        List<NeighborInfo> l2Neighbors = new ArrayList<>();
        NeighborInfo localNeighborInfo = null;
        for (NeighborInfo neighborInfo: neighborInfos) {
            if (neighborInfo.getPortId().equals(fixedIp.getIpAddress())) {
                localNeighborInfo = neighborInfo;
            }else if (neighborInfo.getSubnetId().equals(fixedIp.getSubnetId())) {
                l2Neighbors.add(neighborInfo);
            }
        }

        if (localNeighborInfo == null) {
            localNeighborInfo = getNeighborInfo(context, internalPortEntity, fixedIp);
        }

        List<NeighborEntry> neighborTable = buildNeighborTable(
                localNeighborInfo, l2Neighbors, NeighborEntry.NeighborType.L2);
        context.getNetworkConfig().addNeighborEntries(neighborTable);
    }

    private void setNeighborInfos(PortContext context, InternalPortEntity internalPortEntity) throws Exception {
        List<NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();
        if (internalPortEntity.getFixedIps() == null ||
                neighborInfos == null ||
                internalPortEntity.getBindingHostId() == null) {
            return;
        }

        for (PortEntity.FixedIp fixedIp: internalPortEntity.getFixedIps()) {
            List<String> routerSubnetIds = context.getRouterSubnetIds(internalPortEntity.getVpcId());
            if (routerSubnetIds != null && routerSubnetIds.contains(fixedIp.getSubnetId())) {
                buildL3Neighbors(context, internalPortEntity, fixedIp, routerSubnetIds);
            }

            buildL2Neighbors(context, internalPortEntity, fixedIp);
        }
    }

    private void setTheMissingFields(PortContext context, List<PortEntity> portEntities) throws Exception {
        List<InternalPortEntity> internalPortEntities = context.getNetworkConfig().getPortEntities();
        for (InternalPortEntity internalPortEntity: internalPortEntities) {
            PortEntity portEntity = getPortEntity(portEntities, internalPortEntity.getId());
            if (portEntity == null) {
                LOG.error("Can not find port by id: {}", internalPortEntity.getId());
                throw new PortEntityNotFound();
            }

            if (internalPortEntity.getFixedIps() == null) {
                internalPortEntity.setFixedIps(portEntity.getFixedIps());
            }

            if (internalPortEntity.getMacAddress() == null) {
                internalPortEntity.setMacAddress(portEntity.getMacAddress());
            }

            setNeighborInfos(context, internalPortEntity);
        }

        List<VpcEntity> vpcEntities = context.getNetworkConfig().getVpcEntities();
        List<InternalSubnetEntity> internalSubnetEntities =
                context.getNetworkConfig().getSubnetEntities();

        for (InternalSubnetEntity internalSubnetEntity: internalSubnetEntities) {
            for (VpcEntity vpcEntity: vpcEntities) {
                if (vpcEntity.getId().equals(internalSubnetEntity.getVpcId())) {
                    Integer segmentationId = vpcEntity.getSegmentationId();
                    Long tunnelId = segmentationId != null ? Long.valueOf(segmentationId) : null;
                    internalSubnetEntity.setTunnelId(tunnelId);
                }
            }
        }
    }

    private NetworkConfiguration buildNetworkConfig(PortContext context, List<PortEntity> portEntities) throws Exception {
        /**
         DataPlaneProcessor needs to wait for all previous Processor runs to
         finish before continuing. Since DataPlaneProcessor is the last Processor
         in the process chain, so it can call waitAllRequestsFinish,when calling
         waitAllRequestsFinish we must make sure that all asynchronous methods have been called
         */
        context.getRequestManager().waitAllRequestsFinish();

        NetworkConfig networkConfig = context.getNetworkConfig();
        if (networkConfig.getPortEntities().size() == 0) {
            return null;
        }

        setTheMissingFields(context, portEntities);

        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setVpcs(networkConfig.getVpcEntities());
        networkConfiguration.setSubnets(networkConfig.getSubnetEntities());
        networkConfiguration.setSecurityGroups(networkConfig.getSecurityGroups());
        networkConfiguration.setPortEntities(networkConfig.getPortEntities());
        networkConfiguration.setNeighborInfos(networkConfig.getNeighborInfos());
        networkConfiguration.setNeighborTable(networkConfig.getNeighborTable());

        LOG.info("Network configuration: {}", networkConfiguration);

        return networkConfiguration;
    }

    private void createNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest createNetworkConfigRequest =
                    new CreateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(createNetworkConfigRequest, null);
        }
    }

    private void updateNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest updateNetworkConfigRequest =
                    new UpdateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(updateNetworkConfigRequest, null);
        }
    }

    private void deleteNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest deleteNetworkConfigRequest =
                    new DeleteNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(deleteNetworkConfigRequest, null);
        }
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        createNetworkConfig(context, buildNetworkConfig(context, context.getPortEntities()));
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        updateNetworkConfig(context, buildNetworkConfig(context,
                Collections.singletonList(context.getOldPortEntity())));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        deleteNetworkConfig(context, buildNetworkConfig(context, context.getPortEntities()));
    }
}
