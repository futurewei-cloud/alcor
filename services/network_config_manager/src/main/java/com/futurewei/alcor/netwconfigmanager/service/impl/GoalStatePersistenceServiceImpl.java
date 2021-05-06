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
package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.cache.HostResourceMetadataCache;
import com.futurewei.alcor.netwconfigmanager.cache.ResourceStateCache;
import com.futurewei.alcor.netwconfigmanager.cache.VpcResourceCache;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.entity.VpcResourceMeta;
import com.futurewei.alcor.netwconfigmanager.service.GoalStatePersistenceService;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.cache")
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger();

    @Autowired
    private HostResourceMetadataCache hostResourceMetadataCache;

    @Autowired
    private ResourceStateCache resourceStateCache;

    @Autowired
    private VpcResourceCache vpcResourceCache;

    @Override
    public boolean updateGoalState(String hostId, HostGoalState hostGoalState) throws Exception {

        // TODO: Use Ignite transaction here

        // Step 1: Populate host resource metadata cache
        ResourceMeta existing = hostResourceMetadataCache.getResourceMeta(hostId);
        ResourceMeta latest = NetworkConfigManagerUtil.convertGoalStateToHostResourceMeta(
                hostId, hostGoalState.getGoalState().getHostResourcesMap().get(hostId));
        if (existing == null) {
            hostResourceMetadataCache.addResourceMeta(latest);
        } else {
            ResourceMeta updated = NetworkConfigManagerUtil.consolidateResourceMeta(existing, latest);
            hostResourceMetadataCache.addResourceMeta(updated);
        }

        // Step 2: Populate resource state cache
        Map<String, Integer> vpcIdToVniMap = processVpcStates(hostGoalState);
        processSubnetStates(hostGoalState);
        processPortStates(hostGoalState);
        processDhcpStates(hostGoalState);
        processNeighborStates(hostGoalState);
        processSecurityGroupStates(hostGoalState);
        processRouterStates(hostGoalState);
        processGatewayStates(hostGoalState);

        // Step 3
        populateVpcResourceCache(hostGoalState, vpcIdToVniMap);
        return false;
    }

    private Map<String, Integer> processVpcStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Integer> vpcIdToVniMap = new HashMap<>();
        Map<String, Vpc.VpcState> vpsStatesMap = hostGoalState.getGoalState().getVpcStatesMap();

        for (String resourceId : vpsStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, vpsStatesMap.get(resourceId));
            vpcIdToVniMap.put(resourceId, vpsStatesMap.get(resourceId).getConfiguration().getTunnelId());
        }

        return vpcIdToVniMap;
    }

    private void processSubnetStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Subnet.SubnetState> subnetStatesMap = hostGoalState.getGoalState().getSubnetStatesMap();

        for (String resourceId : subnetStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, subnetStatesMap.get(resourceId));
        }
    }

    private void processPortStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Port.PortState> portStatesMap = hostGoalState.getGoalState().getPortStatesMap();

        for (String resourceId : portStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, portStatesMap.get(resourceId));
        }
    }

    private void processDhcpStates(HostGoalState hostGoalState) throws Exception {
        Map<String, DHCP.DHCPState> dhcpStatesMap = hostGoalState.getGoalState().getDhcpStatesMap();

        for (String resourceId : dhcpStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, dhcpStatesMap.get(resourceId));
        }
    }

    private void processNeighborStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Neighbor.NeighborState> neighborStatesMap = hostGoalState.getGoalState().getNeighborStatesMap();

        for (String resourceId : neighborStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, neighborStatesMap.get(resourceId));
        }
    }

    private void processSecurityGroupStates(HostGoalState hostGoalState) throws Exception {
        Map<String, SecurityGroup.SecurityGroupState> securityGroupStatesMap = hostGoalState.getGoalState().getSecurityGroupStatesMap();

        for (String resourceId : securityGroupStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, securityGroupStatesMap.get(resourceId));
        }
    }

    private void processRouterStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Router.RouterState> routerStatesMap = hostGoalState.getGoalState().getRouterStatesMap();

        for (String resourceId : routerStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, routerStatesMap.get(resourceId));
        }
    }

    private void processGatewayStates(HostGoalState hostGoalState) throws Exception {
        Map<String, Gateway.GatewayState> gatewayStatesMap = hostGoalState.getGoalState().getGatewayStatesMap();

        for (String resourceId : gatewayStatesMap.keySet()) {
            resourceStateCache.addResourceState(resourceId, gatewayStatesMap.get(resourceId));
        }
    }

    private void populateVpcResourceCache(HostGoalState hostGoalState, Map<String, Integer> vpcIdToVniMap) throws Exception {
        Map<String, Port.PortState> portStatesMap = hostGoalState.getGoalState().getPortStatesMap();

        for (String resourceId : portStatesMap.keySet()) {
            Port.PortState portState = portStatesMap.get(resourceId);

            String vpcId = portState.getConfiguration().getVpcId();
            String vni = String.valueOf(vpcIdToVniMap.get(vpcId));
            String portId = portState.getConfiguration().getId();
            String dhcpId = "";
            String routerId = "";
            String gatewayId = "";
            String securityGroupId = "";
            VpcResourceMeta vpcResourceMeta = vpcResourceCache.getResourceMeta(vni);

            for (Port.PortConfiguration.FixedIp fixedIp : portState.getConfiguration().getFixedIpsList()) {
                String subnetId = fixedIp.getSubnetId();
                String portPrivateIp = fixedIp.getIpAddress();

                ResourceMeta portResourceMeta = vpcResourceMeta.getResourceMetas(portPrivateIp);
                if (portResourceMeta == null) {
                    // new port
                    portResourceMeta = new ResourceMeta(portId);
                    portResourceMeta.addVpcId(vpcId)
                            .addSubnetId(subnetId)
                            .addPortId(portId)
                            .addDhcpId(dhcpId)
                            .addRouterId(routerId)
                            .addGatewayId(gatewayId)
                            .addSecurityGroupId(securityGroupId);

                } else {
                    //TODO: handle port metadata consolidation
                }
            }

        }
    }

}
