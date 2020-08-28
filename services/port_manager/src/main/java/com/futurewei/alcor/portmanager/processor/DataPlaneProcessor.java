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

import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.request.CreateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.DeleteNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

    private void setNeighborType(PortContext context, InternalPortEntity internalPortEntity) {
        List<NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();
        if (internalPortEntity.getFixedIps() == null || neighborInfos == null) {
            return;
        }

        Set<String> subnetIds = internalPortEntity.getFixedIps().stream()
                .map(PortEntity.FixedIp::getSubnetId)
                .collect(Collectors.toSet());

        List<String> routerSubnetIds = context.getRouterSubnetIds();

        Iterator<NeighborInfo> iterator = neighborInfos.iterator();
        while (iterator.hasNext()) {
            NeighborInfo neighborInfo = iterator.next();

            if (!routerSubnetIds.contains(neighborInfo.getSubnetId())) {
                iterator.remove();
                continue;
            }

            if (subnetIds.contains(neighborInfo.getSubnetId())) {
                neighborInfo.setNeighborType(NeighborInfo.NeighborType.L3);
            } else {
                neighborInfo.setNeighborType(NeighborInfo.NeighborType.L2);
            }
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

            setNeighborType(context, internalPortEntity);
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
