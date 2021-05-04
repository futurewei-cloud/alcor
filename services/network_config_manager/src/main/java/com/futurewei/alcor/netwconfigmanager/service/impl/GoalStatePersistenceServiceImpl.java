package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.netwconfigmanager.cache.HostResourceMetadataCache;
import com.futurewei.alcor.netwconfigmanager.cache.ResourceStateCache;
import com.futurewei.alcor.netwconfigmanager.cache.VpcResourceCache;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.service.GoalStatePersistenceService;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    @Autowired
    private HostResourceMetadataCache hostResourceMetadataCache;

    @Autowired
    private ResourceStateCache resourceStateCache;

    @Autowired
    private VpcResourceCache vpcResourceCache;

    @Override
    public boolean updateGoalState(String hostId, HostGoalState hostGoalState) throws Exception {

        // TODO: Use Ignite transaction here
        ResourceMeta existing = hostResourceMetadataCache.getResourceMeta(hostId);
        ResourceMeta latest = NetworkConfigManagerUtil.convertGoalStateToHostResourceMeta(
                hostId, hostGoalState.getGoalState().getHostResourcesMap().get(hostId));
        if (existing == null) {
            hostResourceMetadataCache.addResourceMeta(latest);
        } else {
            ResourceMeta updated = NetworkConfigManagerUtil.consolidateResourceMeta(existing, latest);
            hostResourceMetadataCache.addResourceMeta(updated);
        }

        processVpcStates(hostGoalState);
        processSubnetStates(hostGoalState);
        processPortStates(hostGoalState);
        processDhcpStates(hostGoalState);
        processNeighborStates(hostGoalState);
        processSecurityGroupStates(hostGoalState);
        processRouterStates(hostGoalState);
        processGatewayStates(hostGoalState);

        return false;
    }

    private void processVpcStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Vpc.VpcState> vpsStatesMap = hostGoalState.getGoalState().getVpcStatesMap();

        for (String resourceId: vpsStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, vpsStatesMap.get(resourceId));
        }
    }

    private void processSubnetStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Subnet.SubnetState> subnetStatesMap = hostGoalState.getGoalState().getSubnetStatesMap();

        for (String resourceId: subnetStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, subnetStatesMap.get(resourceId));
        }
    }

    private void processPortStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Port.PortState> portStatesMap = hostGoalState.getGoalState().getPortStatesMap();

        for (String resourceId: portStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, portStatesMap.get(resourceId));
        }
    }

    private void processDhcpStates(HostGoalState hostGoalState) throws Exception {
        Map<String, DHCP.DHCPState> dhcpStatesMap = hostGoalState.getGoalState().getDhcpStatesMap();

        for (String resourceId: dhcpStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, dhcpStatesMap.get(resourceId));
        }
    }

    private void processNeighborStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Neighbor.NeighborState> neighborStatesMap = hostGoalState.getGoalState().getNeighborStatesMap();

        for (String resourceId: neighborStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, neighborStatesMap.get(resourceId));
        }
    }

    private void processSecurityGroupStates(HostGoalState hostGoalState) throws Exception {
        Map<String, SecurityGroup.SecurityGroupState> securityGroupStatesMap = hostGoalState.getGoalState().getSecurityGroupStatesMap();

        for (String resourceId: securityGroupStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, securityGroupStatesMap.get(resourceId));
        }
    }

    private void processRouterStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Router.RouterState> routerStatesMap = hostGoalState.getGoalState().getRouterStatesMap();

        for (String resourceId: routerStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, routerStatesMap.get(resourceId));
        }
    }

    private void processGatewayStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Gateway.GatewayState> gatewayStatesMap = hostGoalState.getGoalState().getGatewayStatesMap();

        for (String resourceId: gatewayStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, gatewayStatesMap.get(resourceId));
        }
    }
}
