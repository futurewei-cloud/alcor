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
import java.util.SortedMap;
import java.util.TreeMap;
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
        hostResourceMetadataCache.getTransaction();

        // Step 1: Populate host resource metadata cache
        long t1 = System.currentTimeMillis();
        ResourceMeta existing = hostResourceMetadataCache.getResourceMeta(hostId);
        long t2 = System.currentTimeMillis();
        ResourceMeta latest = NetworkConfigManagerUtil.convertGoalStateToHostResourceMeta(
                hostId, hostGoalState.getGoalState().getHostResourcesMap().get(hostId));
        logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", finished getting resource meta from cache, elapsed time in milliseconds: " + (t2-t1));

        long t3 = 0l;
        long t4 = 0l;
        if (existing == null) {
            t3 = System.currentTimeMillis();
            hostResourceMetadataCache.addResourceMeta(latest);
            t4 = System.currentTimeMillis();
            logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", existing is null, finished adding resource meta from cache, elapsed time in milliseconds: " + (t4-t3));
        } else {
            ResourceMeta updated = NetworkConfigManagerUtil.consolidateResourceMeta(existing, latest);
            t3 = System.currentTimeMillis();
            hostResourceMetadataCache.addResourceMeta(updated);
            t4 = System.currentTimeMillis();
            logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", existing is NOT null, finished adding resource meta from cache, elapsed time in milliseconds: " + (t4-t3));
        }
        long t5 = System.currentTimeMillis();

        // Step 2: Populate resource state cache
        Map<String, Integer> vpcIdToVniMap = processVpcStates(hostGoalState);
        processSubnetStates(hostGoalState);
        processPortStates(hostGoalState);
        processDhcpStates(hostGoalState);
        processNeighborStates(hostGoalState);
        processSecurityGroupStates(hostGoalState);
        processRouterStates(hostGoalState);
        processGatewayStates(hostGoalState);
        long t5_plus = System.currentTimeMillis();
        logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", finished processing goalState, elapsed time in milliseconds: " + (t5_plus-t5));

        // Step 3
        populateVpcResourceCache(hostGoalState, vpcIdToVniMap);
        long t6 = System.currentTimeMillis();
        long t_total = (t6 - t5) + (t4 - t3) + (t2 - t1);
        logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", finished populating vpc resource cache, elapsed time in milliseconds: " + (t6-t5_plus));
        logger.log(Level.FINE, "updateGoalstate : hostId: "+hostId+", total time, elapsed time in milliseconds: " + t_total);

        hostResourceMetadataCache.commit();
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
        SortedMap<String, Subnet.SubnetState> subnetStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getSubnetStatesMap());
        resourceStateCache.addResourceStates(subnetStatesSortedMap);

    }

    private void processPortStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, Port.PortState> portStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getPortStatesMap());
        resourceStateCache.addResourceStates(portStatesSortedMap);

    }

    private void processDhcpStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, DHCP.DHCPState> portStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getDhcpStatesMap());
        resourceStateCache.addResourceStates(portStatesSortedMap);
    }

    private void processNeighborStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, Neighbor.NeighborState> neighborStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getNeighborStatesMap());
        resourceStateCache.addResourceStates(neighborStatesSortedMap);
    }

    private void processSecurityGroupStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, SecurityGroup.SecurityGroupState> securityGroupStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getSecurityGroupStatesMap());
        resourceStateCache.addResourceStates(securityGroupStatesSortedMap);
    }

    private void processRouterStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, Router.RouterState> routerStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getRouterStatesMap());
        resourceStateCache.addResourceStates(routerStatesSortedMap);
    }

    private void processGatewayStates(HostGoalState hostGoalState) throws Exception {
        SortedMap<String, Gateway.GatewayState> gatewayStatesSortedMap = new TreeMap<>(hostGoalState.getGoalState().getGatewayStatesMap());
        resourceStateCache.addResourceStates(gatewayStatesSortedMap);
    }

    private void populateVpcResourceCache(HostGoalState hostGoalState, Map<String, Integer> vpcIdToVniMap) throws Exception {
        long start = System.currentTimeMillis();

        logger.log(Level.FINE, "populateVpcResourceCache : beginning");

        Map<String, Port.PortState> portStatesMap = hostGoalState.getGoalState().getPortStatesMap();
        HashMap<String, VpcResourceMeta> vniToVpcReourceMetaDataMap = new HashMap<>();

        // Retrieve all needed VpcResourceMeta from cache to memory
        for (String resourceId : portStatesMap.keySet()){
            Port.PortState portState = portStatesMap.get(resourceId);
            String vpcId = portState.getConfiguration().getVpcId();
            String vni = String.valueOf(vpcIdToVniMap.get(vpcId));
            // don't get the same vni again.
            if ( ! vniToVpcReourceMetaDataMap.containsKey(vni)){
                VpcResourceMeta vpcResourceMeta = vpcResourceCache.getResourceMeta(vni);
                if (vpcResourceMeta == null) {
                    // This is a new VPC
                    vpcResourceMeta = new VpcResourceMeta(vni, new HashMap<String, ResourceMeta>());
                }
                vniToVpcReourceMetaDataMap.put(vni, vpcResourceMeta);
            }
        }

        // Edit the in-memory VpcResourceData, instead of getting it from cache every time.
        for (String resourceId : portStatesMap.keySet()) {
            Port.PortState portState = portStatesMap.get(resourceId);
            String vpcId = portState.getConfiguration().getVpcId();
            String vni = String.valueOf(vpcIdToVniMap.get(vpcId));
            String portId = portState.getConfiguration().getId();
            String dhcpId = "";  //TODO: support dhcp etc.
            String routerId = "";
            String gatewayId = "";
            String securityGroupId = "";

            VpcResourceMeta vpcResourceMeta = vniToVpcReourceMetaDataMap.get(vni);

            long t3 = System.currentTimeMillis();

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
            long t4 = System.currentTimeMillis();
            logger.log(Level.FINE, "populateVpcResourceCache : looped throught the port states for vpc with vni: "+vni+",  elapsed time in milliseconds: " + (t4 - t3));
            long t5 = System.currentTimeMillis();
//            vpcResourceCache.addResourceMeta(vpcResourceMeta);
            long t6 = System.currentTimeMillis();
            logger.log(Level.FINE, "populateVpcResourceCache : added resource metadata for vpc with vni: "+vni+",  elapsed time in milliseconds: " + (t6 - t5));
        }


        // Commit the changes to the database. This is safe, it is wrapped by a transaction in updateGoalState
        for(String vni : vniToVpcReourceMetaDataMap.keySet()){
            vpcResourceCache.addResourceMeta(vniToVpcReourceMetaDataMap.get(vni));
        }
        long end = System.currentTimeMillis();
        logger.log(Level.FINE, "populateVpcResourceCache : end,  elapsed time in milliseconds: " + (end-start));
    }

}
