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

import com.futurewei.alcor.common.enumClass.StatusEnum;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetNodeInfoException;
import com.futurewei.alcor.portmanager.exception.NodeInfoNotFound;
import com.futurewei.alcor.portmanager.exception.PortEntityNotFound;
import com.futurewei.alcor.portmanager.request.CreateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.DeleteNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@AfterProcessor({FixedIpsProcessor.class, MacProcessor.class,
        NeighborProcessor.class, NodeProcessor.class, PortProcessor.class,
        RouterProcessor.class, SecurityGroupProcessor.class, VpcProcessor.class})
public class DataPlaneProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneProcessor.class);

    private PortEntity getPortEntity(List<PortEntity> portEntities, String portId) {
        for (PortEntity portEntity : portEntities) {
            if (portEntity.getId().equals(portId)) {
                return portEntity;
            }
        }

        return null;
    }

    private List<NeighborEntry> buildNeighborEntries(NeighborInfo localInfo, List<NeighborInfo> neighbors, NeighborEntry.NeighborType neighborType) {
        List<NeighborEntry> neighborTable = new ArrayList<>();
        for (NeighborInfo neighbor : neighbors) {
            NeighborEntry neighborEntry = new NeighborEntry();
            neighborEntry.setNeighborType(neighborType);
            neighborEntry.setLocalIp(localInfo.getPortIp());
            neighborEntry.setNeighborIp(neighbor.getPortIp());
            neighborTable.add(neighborEntry);
        }

        return neighborTable;
    }

    private NeighborInfo getNeighborInfo(PortContext context, InternalPortEntity internalPortEntity, PortEntity.FixedIp fixedIp) throws Exception {
        List<NodeInfo> nodeInfos = context.getNodeInfos();
        if (nodeInfos == null) {
            throw new NodeInfoNotFound();
        }

        NeighborInfo neighborInfo = null;
        for (NodeInfo nodeInfo : nodeInfos) {
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
        Map<String, NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();
        if (neighborInfos == null) {
            return;
        }

        List<NeighborInfo> l3Neighbors = new ArrayList<>();
        NeighborInfo localNeighborInfo = null;
        for (Map.Entry<String, NeighborInfo> entry : neighborInfos.entrySet()) {
            NeighborInfo neighborInfo = entry.getValue();
            if (neighborInfo.getPortIp().equals(fixedIp.getIpAddress())) {
                localNeighborInfo = neighborInfo;
            } else if (routerSubnetIds.contains(neighborInfo.getSubnetId()) &&
                    !neighborInfo.getSubnetId().equals(fixedIp.getSubnetId())) {
                l3Neighbors.add(neighborInfo);
            }
        }

        if (localNeighborInfo == null) {
            localNeighborInfo = getNeighborInfo(context, internalPortEntity, fixedIp);
        }

        String portIp = localNeighborInfo.getPortIp();
        List<NeighborEntry> neighborEntries = buildNeighborEntries(
                localNeighborInfo, l3Neighbors, NeighborEntry.NeighborType.L3);
        context.getNetworkConfig().addNeighborEntries(portIp, neighborEntries);
    }

    private void buildL2Neighbors(PortContext context, InternalPortEntity internalPortEntity, PortEntity.FixedIp fixedIp) throws Exception {
        Map<String, NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();

        List<NeighborInfo> l2Neighbors = new ArrayList<>();
        NeighborInfo localNeighborInfo = null;
        for (Map.Entry<String, NeighborInfo> entry : neighborInfos.entrySet()) {
            NeighborInfo neighborInfo = entry.getValue();
            if (neighborInfo.getPortIp().equals(fixedIp.getIpAddress())) {
                localNeighborInfo = neighborInfo;
            } else if (neighborInfo.getSubnetId().equals(fixedIp.getSubnetId())) {
                l2Neighbors.add(neighborInfo);
            }
        }

        if (localNeighborInfo == null) {
            localNeighborInfo = getNeighborInfo(context, internalPortEntity, fixedIp);
        }

        String portIp = localNeighborInfo.getPortIp();
        List<NeighborEntry> neighborTable = buildNeighborEntries(
                localNeighborInfo, l2Neighbors, NeighborEntry.NeighborType.L2);
        context.getNetworkConfig().addNeighborEntries(portIp, neighborTable);
    }

    private void setNeighborInfos(PortContext context, InternalPortEntity internalPortEntity) throws Exception {
        Map<String, NeighborInfo> neighborInfos = context.getNetworkConfig().getNeighborInfos();
        if (internalPortEntity.getFixedIps() == null ||
                neighborInfos == null ||
                internalPortEntity.getBindingHostId() == null) {
            return;
        }

        for (PortEntity.FixedIp fixedIp : internalPortEntity.getFixedIps()) {
            List<SubnetEntity> routerSubnetEntities = context.getRouterSubnetEntities(internalPortEntity.getVpcId());
            if (routerSubnetEntities != null && routerSubnetEntities.size() > 0) {
                List<String> routerSubnetIds = new ArrayList<>();
                for (SubnetEntity entity : routerSubnetEntities) {
                    routerSubnetIds.add(entity.getId());
                }

                if (routerSubnetIds.contains(fixedIp.getSubnetId())) {
                    buildL3Neighbors(context, internalPortEntity, fixedIp, routerSubnetIds);
                }
            }

            buildL2Neighbors(context, internalPortEntity, fixedIp);
        }
    }

    private void setTheMissingFields(PortContext context, List<PortEntity> portEntities) throws Exception {
        List<InternalPortEntity> internalPortEntities = context.getNetworkConfig().getPortEntities();
        for (InternalPortEntity internalPortEntity : internalPortEntities) {
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

            if (internalPortEntity.getSecurityGroups() == null) {
                internalPortEntity.setSecurityGroups(portEntity.getSecurityGroups());
            }

            setNeighborInfos(context, internalPortEntity);
        }

        List<VpcEntity> vpcEntities = context.getNetworkConfig().getVpcEntities();
        for (VpcEntity vpcEntity : vpcEntities) {
            InternalRouterInfo router = context.getRouterByVpcOrSubnetId(vpcEntity.getId());
            if (router == null || router.getRouterConfiguration() == null) continue;

            // Set router information
            // NOTE: This implementation support Neutron scenario only
            context.getNetworkConfig().addRouterEntry(router);

            // Add associated subnet entities
            List<SubnetEntity> associatedSubnetEntities = context.getRouterSubnetEntities(vpcEntity.getId());
            for (SubnetEntity entity : associatedSubnetEntities) {
                InternalSubnetEntity internalEntity = new InternalSubnetEntity(entity, Long.MAX_VALUE);
                context.getNetworkConfig().addSubnetEntity(internalEntity);
            }
        }

        // Set Tunnel Ids in internal Subnet entities as assigned by control plane
        List<InternalSubnetEntity> internalSubnetEntities =
                context.getNetworkConfig().getSubnetEntities();

        if (internalSubnetEntities != null) {
            for (InternalSubnetEntity internalSubnetEntity : internalSubnetEntities) {
                for (VpcEntity vpcEntity : vpcEntities) {
                    if (vpcEntity.getId().equals(internalSubnetEntity.getVpcId())) {
                        Integer segmentationId = vpcEntity.getSegmentationId();
                        Long tunnelId = segmentationId != null ? Long.valueOf(segmentationId) : null;
                        internalSubnetEntity.setTunnelId(tunnelId);
                    }
                }
            }
        }

    }

    private List<ResourceOperation> initializeResourceOperationTypes(PortContext context, List<PortEntity> portEntities) {
        List<ResourceOperation> resourceOperationTypes = new ArrayList<>();
        resourceOperationTypes.add(new ResourceOperation(Common.ResourceType.PORT, Common.OperationType.CREATE));
        resourceOperationTypes.add(new ResourceOperation(Common.ResourceType.NEIGHBOR, Common.OperationType.CREATE));

        // TODO: to enable full sg support, we will need to add full security group entities to context and check context here
        for (PortEntity port : portEntities) {
            if (!CommonUtil.isNullOrEmpty(context.getDefaultSgId()) && port.getPortSecurityEnabled()) {
                resourceOperationTypes.add(new ResourceOperation(Common.ResourceType.SECURITYGROUP, Common.OperationType.CREATE));
                break;
            }
        }

        if (context.containRouters()) {
            resourceOperationTypes.add(new ResourceOperation(Common.ResourceType.NEIGHBOR, Common.OperationType.CREATE));
        }

        return resourceOperationTypes;
    }

    private void markOperationTypes(NetworkConfiguration networkConfig, Common.OperationType operationType) {
        if (networkConfig == null || networkConfig.getRsOpTypes() == null) {
            return;
        }

        List<ResourceOperation> resourceOperationTypes = networkConfig.getRsOpTypes();
        for (ResourceOperation type : resourceOperationTypes) {
            type.setOpType(operationType);
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
        if (networkConfig.getPortEntities() == null
                || networkConfig.getPortEntities().size() == 0) {
            return null;
        }

        setTheMissingFields(context, portEntities);
        List<ResourceOperation> resourceOperationTypes = initializeResourceOperationTypes(context, portEntities);

        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setRsType(Common.ResourceType.PORT);
        networkConfiguration.setRsOpTypes(resourceOperationTypes);
        networkConfiguration.setVpcs(networkConfig.getVpcEntities());
        networkConfiguration.setSubnets(networkConfig.getSubnetEntities());
        networkConfiguration.setSecurityGroups(networkConfig.getSecurityGroups());
        networkConfiguration.setPortEntities(networkConfig.getPortEntities());
        networkConfiguration.setNeighborInfos(networkConfig.getNeighborInfos());
        networkConfiguration.setNeighborTable(networkConfig.getNeighborTable());
        networkConfiguration.setInternalRouterInfos(networkConfig.getRouterInfos());

        LOG.info("Network configuration: {}", networkConfiguration);

        return networkConfiguration;
    }

    private void createNetworkConfig(PortContext context, NetworkConfiguration networkConfig) throws Exception {
        PortService portService = SpringContextUtil.getBean(PortService.class);
        if (networkConfig != null) {
            networkConfig.setOpType(Common.OperationType.CREATE);
            IRestRequest createNetworkConfigRequest =
                    new CreateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(createNetworkConfigRequest, request -> portService.updatePortStatus(request, networkConfig, null));
            portService.updatePortStatus(createNetworkConfigRequest, networkConfig, StatusEnum.CREATED.getStatus());
        }
    }

    private void updateNetworkConfig(PortContext context, NetworkConfiguration networkConfig) throws Exception {
        PortService portService = SpringContextUtil.getBean(PortService.class);
        if (networkConfig != null) {
            networkConfig.setOpType(Common.OperationType.UPDATE);
            IRestRequest updateNetworkConfigRequest =
                    new UpdateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(updateNetworkConfigRequest, request -> portService.updatePortStatus(request, networkConfig, null));
            portService.updatePortStatus(updateNetworkConfigRequest, networkConfig, StatusEnum.PENDING.getStatus());
        }
    }

    private void deleteNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            networkConfig.setOpType(Common.OperationType.DELETE);
            markOperationTypes(networkConfig, Common.OperationType.DELETE);
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
                Collections.singletonList(context.getNewPortEntity())));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        deleteNetworkConfig(context, buildNetworkConfig(context, context.getPortEntities()));
    }
}
