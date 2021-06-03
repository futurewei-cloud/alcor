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
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
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
import java.util.logging.Level;

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
    @DurationStatistics
    public boolean updateGoalState(String hostId, HostGoalState hostGoalState) throws Exception {

        // TODO: Use Ignite transaction here

        // Step 1: Populate host resource metadata cache
        Long t1 = System.currentTimeMillis();
        ResourceMeta existing = hostResourceMetadataCache.getResourceMeta(hostId);
        Long t2 = System.currentTimeMillis();
        ResourceMeta latest = NetworkConfigManagerUtil.convertGoalStateToHostResourceMeta(
                hostId, hostGoalState.getGoalState().getHostResourcesMap().get(hostId));
        logger.log(Level.INFO, "updateGoalstate : hostId: "+hostId+", finished getting resource meta from cache, elapsed time in milliseconds: " + (t2-t1));

        Long t3 = 0l;
        Long t4 = 0l;
        if (existing == null) {
            t3 = System.currentTimeMillis();
            hostResourceMetadataCache.addResourceMeta(latest);
            t4 = System.currentTimeMillis();
            logger.log(Level.INFO, "updateGoalstate : hostId: "+hostId+", existing is null, finished adding resource meta from cache, elapsed time in milliseconds: " + (t4-t3));
        } else {
            ResourceMeta updated = NetworkConfigManagerUtil.consolidateResourceMeta(existing, latest);
            t3 = System.currentTimeMillis();
            hostResourceMetadataCache.addResourceMeta(updated);
            t4 = System.currentTimeMillis();
            logger.log(Level.INFO, "updateGoalstate : hostId: "+hostId+", existing is NOT null, finished adding resource meta from cache, elapsed time in milliseconds: " + (t4-t3));
        }
        Long t5 = System.currentTimeMillis();

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
        Long t6 = System.currentTimeMillis();
        Long t_total = (t6 - t5) + (t4 - t3) + (t2 - t1);
        logger.log(Level.INFO, "updateGoalstate : hostId: "+hostId+", finished populating vpc resource cache, elapsed time in milliseconds: " + (t6-t5));
        logger.log(Level.INFO, "updateGoalstate : hostId: "+hostId+", total time, elapsed time in milliseconds: " + t_total);

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
            String dhcpId = "";  //TODO: support dhcp etc.
            String routerId = "";
            String gatewayId = "";
            String securityGroupId = "";

            VpcResourceMeta vpcResourceMeta = vpcResourceCache.getResourceMeta(vni);
            if (vpcResourceMeta == null) {
                // This is a new VPC
                vpcResourceMeta = new VpcResourceMeta(vni, new HashMap<String, ResourceMeta>());
            }

            for (Port.PortConfiguration.FixedIp fixedIp : portState.getConfiguration().getFixedIpsList()) {
                String subnetId = fixedIp.getSubnetId();
                String portPrivateIp = fixedIp.getIpAddress();

                ResourceMeta portResourceMeta = vpcResourceMeta.getResourceMeta(portPrivateIp);
                if (portResourceMeta == null) {
                    // new port
                    portResourceMeta = new ResourceMeta(portId);
                } else {
                    //TODO: handle port metadata consolidation including cleanup of legacy metadata
                }

                if (!CommonUtil.isNullOrEmpty(vpcId)) portResourceMeta.addVpcId(vpcId);
                if (!CommonUtil.isNullOrEmpty(subnetId)) portResourceMeta.addSubnetId(subnetId);
                if (!CommonUtil.isNullOrEmpty(portId)) portResourceMeta.addPortId(portId);
                if (!CommonUtil.isNullOrEmpty(dhcpId)) portResourceMeta.addDhcpId(dhcpId);
                if (!CommonUtil.isNullOrEmpty(routerId)) portResourceMeta.addRouterId(routerId);
                if (!CommonUtil.isNullOrEmpty(gatewayId)) portResourceMeta.addGatewayId(gatewayId);
                if (!CommonUtil.isNullOrEmpty(securityGroupId)) portResourceMeta.addSecurityGroupId(securityGroupId);

                vpcResourceMeta.setResourceMeta(portPrivateIp, portResourceMeta);
            }

            vpcResourceCache.addResourceMeta(vpcResourceMeta);
        }
    }

}
