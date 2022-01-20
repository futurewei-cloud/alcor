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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.dataplane.cache.NeighborCache;
import com.futurewei.alcor.dataplane.cache.PortHostInfoCache;
import com.futurewei.alcor.dataplane.cache.SubnetPortsCacheV2;
import com.futurewei.alcor.dataplane.entity.*;
import com.futurewei.alcor.dataplane.exception.NeighborInfoNotFound;
import com.futurewei.alcor.dataplane.exception.PortFixedIpNotFound;
import com.futurewei.alcor.schema.*;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NeighborService extends ResourceService {

    @Autowired
    private PortHostInfoCache portHostInfoCache;

    @Autowired
    private SubnetPortsCacheV2 subnetPortsCache;

    @Autowired
    private NeighborCache neighborCache;

    @Autowired
    private SubnetService subnetService;

    @Autowired
    private RouterService routerService;

    public Neighbor.NeighborState buildNeighborState(NeighborEntry.NeighborType type, NeighborInfo neighborInfo, Common.OperationType operationType) throws Exception {
        Neighbor.NeighborConfiguration.Builder neighborConfigBuilder = Neighbor.NeighborConfiguration.newBuilder();
        neighborConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
        neighborConfigBuilder.setId(neighborInfo.getPortId()); // TODO: We are going to need this per latest ACA change
        neighborConfigBuilder.setVpcId(neighborInfo.getVpcId());
        //neighborConfigBuilder.setName();
        neighborConfigBuilder.setMacAddress(neighborInfo.getPortMac());
        neighborConfigBuilder.setHostIpAddress(neighborInfo.getHostIp());
        Neighbor.NeighborType neighborType = Neighbor.NeighborType.valueOf(type.getType());

        //TODO:setNeighborHostDvrMac
        //neighborConfigBuilder.setNeighborHostDvrMac();
        Neighbor.NeighborConfiguration.FixedIp.Builder fixedIpBuilder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
        fixedIpBuilder.setSubnetId(neighborInfo.getSubnetId());
        fixedIpBuilder.setIpAddress(neighborInfo.getPortIp());
        fixedIpBuilder.setNeighborType(neighborType);
        neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
        //TODO:setAllowAddressPairs
        //neighborConfigBuilder.setAllowAddressPairs();

        Neighbor.NeighborState.Builder neighborStateBuilder = Neighbor.NeighborState.newBuilder();
        neighborStateBuilder.setOperationType(operationType);
        neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());
        neighborCache.setNeighborState(neighborStateBuilder.build());
        return neighborStateBuilder.build();
    }

    public Neighbor.NeighborState buildNeighborState(NeighborEntry.NeighborType type, PortHostInfo portHostInfo, Common.OperationType operationType, String vpcId) throws Exception {
        Neighbor.NeighborConfiguration.Builder neighborConfigBuilder = Neighbor.NeighborConfiguration.newBuilder();
        neighborConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
        neighborConfigBuilder.setId(UUID.randomUUID().toString()); // TODO: We are going to need this per latest ACA change
        neighborConfigBuilder.setVpcId(vpcId);
        //neighborConfigBuilder.setName();
        neighborConfigBuilder.setMacAddress(portHostInfo.getPortMac());
        neighborConfigBuilder.setHostIpAddress(portHostInfo.getHostIp());
        Neighbor.NeighborType neighborType = Neighbor.NeighborType.valueOf(type.getType());

        //TODO:setNeighborHostDvrMac
        //neighborConfigBuilder.setNeighborHostDvrMac();
        Neighbor.NeighborConfiguration.FixedIp.Builder fixedIpBuilder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
        fixedIpBuilder.setSubnetId(portHostInfo.getSubnetId());
        fixedIpBuilder.setIpAddress(portHostInfo.getPortIp());
        fixedIpBuilder.setNeighborType(neighborType);
        neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
        //TODO:setAllowAddressPairs
        //neighborConfigBuilder.setAllowAddressPairs();

        Neighbor.NeighborState.Builder neighborStateBuilder = Neighbor.NeighborState.newBuilder();
        neighborStateBuilder.setOperationType(operationType);
        neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());
        neighborCache.setNeighborState(neighborStateBuilder.build());
        return neighborStateBuilder.build();
    }

    private List<NeighborInfo> buildNeighborInfosByPortEntities(NetworkConfiguration networkConfig) {
        List<NeighborInfo> neighborInfos = new ArrayList<>();

        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        if (internalPortEntities != null) {
            for (InternalPortEntity internalPortEntity: internalPortEntities) {
                String bindingHostIP = internalPortEntity.getBindingHostIP();
                if (bindingHostIP == null) {
                    continue;
                }

                for (PortEntity.FixedIp fixedIp: internalPortEntity.getFixedIps()) {
                    NeighborInfo neighborInfo = new NeighborInfo(bindingHostIP,
                            internalPortEntity.getBindingHostId(),
                            internalPortEntity.getId(),
                            internalPortEntity.getMacAddress(),
                            fixedIp.getIpAddress(),
                            internalPortEntity.getVpcId(),
                            fixedIp.getSubnetId());
                    neighborInfos.add(neighborInfo);
                }
            }
        }

        return neighborInfos;
    }

    public void buildNeighborStates(Map<String, NeighborInfo> neighborInfos,
                                    UnicastGoalState unicastGoalState,
                                    MulticastGoalState multicastGoalState) throws Exception
    {
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }
        for (NeighborInfo neighborInfo : neighborInfos.values())
        {
            unicastGoalState.getGoalStateBuilder().addNeighborStates(buildNeighborState(NeighborEntry.NeighborType.L3, neighborInfo, Common.OperationType.GET));
        }

    }

    public void buildNeighborStates(NetworkConfiguration networkConfig, String hostIp,
                                     UnicastGoalState unicastGoalState,
                                     MulticastGoalState multicastGoalState) throws Exception {
        Map<String, NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }

        /**
         * PortEntities themselves are not included in neighborInfos, build neighborInfos
         * for them and add them to neighborInfo map before building neighborStates
         */
        List<NeighborInfo> neighborInfoList = buildNeighborInfosByPortEntities(networkConfig);
        for (NeighborInfo neighborInfo: neighborInfoList) {
            if (!neighborInfos.containsKey(neighborInfo.getPortIp())) {
                neighborInfos.put(neighborInfo.getPortIp(), neighborInfo);
            }
        }

        Map<String, List<NeighborEntry>> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<NeighborEntry> multicastNeighborEntries = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            if (fixedIps == null) {
                throw new PortFixedIpNotFound();
            }

            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                List<NeighborEntry> neighborEntries = neighborTable.get(fixedIp.getIpAddress());
                if (neighborEntries == null) {
                    throw new NeighborInfoNotFound();
                }

                for (NeighborEntry neighborEntry: neighborEntries) {
                    NeighborInfo neighborInfo = neighborInfos.get(neighborEntry.getNeighborIp());
                    if (neighborInfo == null) {
                        throw new NeighborInfoNotFound();
                    }

                    if (hostIp.equals(neighborInfo.getHostIp()) && !neighborEntry.getNeighborType().equals(NeighborEntry.NeighborType.L3)) {
                        continue;
                    }

                    unicastGoalState.getGoalStateBuilder().addNeighborStates(buildNeighborState(
                            neighborEntry.getNeighborType(), neighborInfo, networkConfig.getOpType()));
                    multicastNeighborEntries.add(neighborEntry);
                }
            }
        }

        Set<NeighborInfo> neighborInfoSet = new HashSet<>();
        for (NeighborEntry neighborEntry: multicastNeighborEntries) {
            String localIp = neighborEntry.getLocalIp();
            String neighborIp = neighborEntry.getNeighborIp();
            NeighborInfo neighborInfo1 = neighborInfos.get(localIp);
            NeighborInfo neighborInfo2 = neighborInfos.get(neighborIp);
            if (neighborInfo1 == null || neighborInfo2 == null) {
                throw new NeighborInfoNotFound();
            }

            if (!multicastGoalState.getHostIps().contains(neighborInfo2.getHostIp())) {
                multicastGoalState.getHostIps().add(neighborInfo2.getHostIp());
            }

            if (!neighborInfoSet.contains(neighborInfo1)) {
                multicastGoalState.getGoalStateBuilder().addNeighborStates(buildNeighborState(
                        neighborEntry.getNeighborType(), neighborInfo1, networkConfig.getOpType()));
                neighborInfoSet.add(neighborInfo1);
            }
        }
    }

    public void buildNeighborStates(NetworkConfiguration networkConfig, String hostIp,
                                    UnicastGoalStateV2 unicastGoalState,
                                    MulticastGoalStateV2 multicastGoalState) throws Exception {
        Map<String, NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        if (neighborInfos == null || neighborInfos.size() == 0) {
            return;
        }

        /**
         * PortEntities themselves are not included in neighborInfos, build neighborInfos
         * for them and add them to neighborInfo map before building neighborStates
         */
        List<NeighborInfo> neighborInfoList = buildNeighborInfosByPortEntities(networkConfig);
        for (NeighborInfo neighborInfo: neighborInfoList) {
            if (!neighborInfos.containsKey(neighborInfo.getPortIp())) {
                neighborInfos.put(neighborInfo.getPortIp(), neighborInfo);
            }
        }

        Map<String, List<NeighborEntry>> neighborTable = networkConfig.getNeighborTable();
        if (neighborTable == null || neighborTable.size() == 0) {
            return;
        }

        List<Port.PortState> portStates = new ArrayList<Port.PortState>(unicastGoalState.getGoalStateBuilder().getPortStatesMap().values());
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<NeighborEntry> multicastNeighborEntries = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            if (fixedIps == null) {
                throw new PortFixedIpNotFound();
            }

            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                List<NeighborEntry> neighborEntries = neighborTable.get(fixedIp.getIpAddress());
                if (neighborEntries == null) {
                    throw new NeighborInfoNotFound();
                }

                for (NeighborEntry neighborEntry: neighborEntries) {
                    NeighborInfo neighborInfo = neighborInfos.get(neighborEntry.getNeighborIp());
                    if (neighborInfo == null) {
                        throw new NeighborInfoNotFound();
                    }

                    if (hostIp.equals(neighborInfo.getHostIp()) && !neighborEntry.getNeighborType().equals(NeighborEntry.NeighborType.L3)) {
                        continue;
                    }

                    Neighbor.NeighborState neighborState = buildNeighborState(neighborEntry.getNeighborType(), neighborInfo, networkConfig.getOpType());
                    unicastGoalState.getGoalStateBuilder().putNeighborStates(neighborState.getConfiguration().getId(), neighborState);
                    multicastNeighborEntries.add(neighborEntry);
                }
            }
        }

        Set<NeighborInfo> neighborInfoSet = new HashSet<>();
        for (NeighborEntry neighborEntry: multicastNeighborEntries) {
            String localIp = neighborEntry.getLocalIp();
            String neighborIp = neighborEntry.getNeighborIp();
            NeighborInfo neighborInfo1 = neighborInfos.get(localIp);
            NeighborInfo neighborInfo2 = neighborInfos.get(neighborIp);
            if (neighborInfo1 == null || neighborInfo2 == null) {
                throw new NeighborInfoNotFound();
            }

            if (!multicastGoalState.getHostIps().contains(neighborInfo2.getHostIp())) {
                multicastGoalState.getHostIps().add(neighborInfo2.getHostIp());
            }

            if (!neighborInfoSet.contains(neighborInfo1)) {
                Neighbor.NeighborState neighborState = buildNeighborState(neighborEntry.getNeighborType(), neighborInfo1, networkConfig.getOpType());
                multicastGoalState.getGoalStateBuilder().putNeighborStates(neighborState.getConfiguration().getId(), neighborState);

                neighborInfoSet.add(neighborInfo1);
            }
        }
        }

    public List<Neighbor.NeighborState> getAllNeighbors (Set<String> ips) throws Exception
    {
        List<Neighbor.NeighborState> neighbors = new ArrayList<>();
        for (String ip : ips)
        {
            neighbors.add(neighborCache.getNeiborByIP(ip));

        }
        return neighbors;
    }

    public void buildNeighborStatesL2(UnicastGoalStateV2 unicastGoalStateV2, MulticastGoalStateV2 multicastGoalStateV2, Common.OperationType operationType) {
        Collection<Port.PortState> portStates = unicastGoalStateV2.getGoalStateBuilder().getPortStatesMap().values();
        Map<String, Neighbor.NeighborState> neighborStateMap = new TreeMap<>();

        Set<String> ips = portStates
                .stream()
                .flatMap(portState -> portState.getConfiguration().getFixedIpsList().stream().map(fixedIp -> fixedIp.getIpAddress()))
                .collect(Collectors.toSet());

        multicastGoalStateV2.getGoalStateBuilder().putAllSubnetStates(unicastGoalStateV2.getGoalStateBuilder().getSubnetStatesMap());
        multicastGoalStateV2.getGoalStateBuilder().putAllRouterStates(unicastGoalStateV2.getGoalStateBuilder().getRouterStatesMap());

        portStates.parallelStream().forEach(portState -> {
            List<String> subnetIds = portState.getConfiguration().getFixedIpsList().stream().map(fixedIp -> fixedIp.getSubnetId()).collect(Collectors.toList());

            for (String subnetId : subnetIds) {
                try {
                    Collection<PortHostInfo> portHostInfos = portHostInfoCache.getPortHostInfos(subnetId);

                    portHostInfos.parallelStream().forEach(portHostInfo -> {
                        try {
                            Neighbor.NeighborState neighborState = buildNeighborState(NeighborEntry.NeighborType.L2, portHostInfo, operationType, portState.getConfiguration().getVpcId());
                            if (ips.contains(portHostInfo.getPortIp())) {
                                multicastGoalStateV2.getGoalStateBuilder().putNeighborStates(neighborState.getConfiguration().getId(), neighborState);
                            } else {
                                multicastGoalStateV2.getHostIps().add(portHostInfo.getHostIp());
                                neighborStateMap.put(neighborState.getConfiguration().getId(), neighborState);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } catch (CacheException e) {
                    e.printStackTrace();
                }
            }
        });
        unicastGoalStateV2.getGoalStateBuilder().putAllNeighborStates(neighborStateMap);
        if (multicastGoalStateV2.getHostIps().size() == 0){
            multicastGoalStateV2.getGoalStateBuilder().clear();
        }
    }

    public void buildNeighborStatesL3(NetworkConfiguration networkConfiguration, UnicastGoalStateV2 unicastGoalStateV2, MulticastGoalStateV2 multicastGoalStateV2) {
        Collection<Port.PortState> portStates = unicastGoalStateV2.getGoalStateBuilder().getPortStatesMap().values();
        Map<String, Neighbor.NeighborState> neighborStateMap = new TreeMap<>();
        Map<String, Subnet.SubnetState> subnetStateMap = new TreeMap<>();
        Set<String> ips = portStates
                .stream()
                .flatMap(portState -> portState.getConfiguration().getFixedIpsList().stream().map(fixedIp -> fixedIp.getIpAddress()))
                .collect(Collectors.toSet());

        Set<String> subnetIds = portStates
                .stream()
                .flatMap(portState -> portState.getConfiguration().getFixedIpsList().stream().map(fixedIp -> fixedIp.getSubnetId()))
                .collect(Collectors.toSet());

        multicastGoalStateV2.getGoalStateBuilder().putAllSubnetStates(unicastGoalStateV2.getGoalStateBuilder().getSubnetStatesMap());
        multicastGoalStateV2.getGoalStateBuilder().putAllRouterStates(unicastGoalStateV2.getGoalStateBuilder().getRouterStatesMap());

        Set<String> routerIds = subnetPortsCache.getInternalSubnetRouterMap(networkConfiguration).values().stream().collect(Collectors.toSet());
        String vpcid = unicastGoalStateV2.getGoalStateBuilder().getVpcStatesMap().values().stream().map(vpcState -> vpcState.getConfiguration().getId()).findFirst().orElse(null);
        routerIds.parallelStream().forEach(routerId -> {
            try {
                Collection<InternalSubnetPorts> internalSubnetPorts  = subnetPortsCache.getSubnetPortsByRouterId(routerId).values();
                for (InternalSubnetPorts internalSubnetPort : internalSubnetPorts) {
                    try {
                        Collection<PortHostInfo> portHostInfos = portHostInfoCache.getPortHostInfos(internalSubnetPort.getSubnetId());
                        portHostInfos.parallelStream().forEach(portHostInfo -> {
                            try {
                                Neighbor.NeighborState neighborState = buildNeighborState(NeighborEntry.NeighborType.L3, portHostInfo, networkConfiguration.getOpType(), vpcid);
                                if (subnetIds.contains(internalSubnetPort.getSubnetId()) && ips.contains(portHostInfo.getPortIp())) {
                                    multicastGoalStateV2.getGoalStateBuilder().putNeighborStates(neighborState.getConfiguration().getId(), neighborState);
                                } else {
                                    multicastGoalStateV2.getHostIps().add(portHostInfo.getHostIp());
                                    neighborStateMap.put(neighborState.getConfiguration().getId(), neighborState);
                                    subnetStateMap.put(internalSubnetPort.getSubnetId(), subnetService.buildSubnetState(internalSubnetPort.getSubnetId()).build());
                                    if (!unicastGoalStateV2.getGoalStateBuilder().getSubnetStatesMap().containsKey(internalSubnetPort.getSubnetId())){
                                        Router.RouterConfiguration.SubnetRoutingTable.Builder subnetRoutingTableBuilder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
                                        String subnetId = portHostInfo.getSubnetId();
                                        subnetRoutingTableBuilder.setSubnetId(subnetId);
                                        Router.RouterConfiguration.Builder routerConfigurationBuilder =  unicastGoalStateV2.getGoalStateBuilder().getRouterStatesMap().get(routerId).getConfiguration().toBuilder();
                                        routerConfigurationBuilder.addSubnetRoutingTables(subnetRoutingTableBuilder.build());
                                        unicastGoalStateV2.getGoalStateBuilder().putRouterStates(routerId, Router.RouterState.newBuilder().setConfiguration(routerConfigurationBuilder).build());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (CacheException e) {
                        e.printStackTrace();
                    }
                }
            } catch (CacheException e) {
                e.printStackTrace();
            }
        });
        unicastGoalStateV2.getGoalStateBuilder().putAllNeighborStates(neighborStateMap);
        unicastGoalStateV2.getGoalStateBuilder().putAllSubnetStates(subnetStateMap);
    }


    public void buildNeighborStatesL3(NetworkConfiguration networkConfiguration, Map<String, UnicastGoalStateV2> unicastGoalStates,  MulticastGoalStateV2 multicastGoalState) throws Exception {
        String routerId = networkConfiguration.getInternalRouterInfos().get(0).getRouterConfiguration().getId();
        Map<String, Neighbor.NeighborState> neighborStateMap = new TreeMap<>();
        Map<String, Subnet.SubnetState> subnetStateMap = new TreeMap<>();
        Router.RouterConfiguration.Builder unicastRouterConfigurationBuilder = Router.RouterConfiguration.newBuilder();
        if (networkConfiguration.getSubnets().size() == 0) {
            return;
        }
        String subnetId = networkConfiguration.getSubnets().stream().map(subnetEntity -> subnetEntity.getId()).findFirst().orElse("");
        if (subnetPortsCache.getSubnetPorts(subnetId) == null) {
            return;
        }
        String vpcid = subnetPortsCache.getSubnetPorts(subnetId).getVpcId();
        portHostInfoCache.getPortHostInfos(subnetId)
                .forEach(portState -> unicastGoalStates.put(portState.getHostIp(), new UnicastGoalStateV2(portState.getHostIp(), Goalstate.GoalStateV2.newBuilder())));
        Subnet.SubnetState subnetState = subnetService.buildSubnetState(subnetId).build();
        Collection<InternalSubnetPorts> internalSubnetPorts  = subnetPortsCache.getSubnetPortsByRouterId(routerId).values();
        for (InternalSubnetPorts internalSubnetPort : internalSubnetPorts) {
            try {
                Collection<PortHostInfo> portHostInfos = portHostInfoCache.getPortHostInfos(internalSubnetPort.getSubnetId());
                portHostInfos.parallelStream().forEach(portHostInfo -> {
                    try {
                        Neighbor.NeighborState neighborState = buildNeighborState(NeighborEntry.NeighborType.L3, portHostInfo, networkConfiguration.getOpType(), vpcid);
                        if (subnetId.equals(internalSubnetPort.getSubnetId())) {
                            multicastGoalState.getGoalStateBuilder().putNeighborStates(neighborState.getConfiguration().getId(), neighborState);
                            if (!multicastGoalState.getGoalStateBuilder().getSubnetStatesMap().containsKey(subnetId)) {
                                InternalSubnetRoutingTable subnetRoutingTables =
                                        networkConfiguration.getInternalRouterInfos().get(0).getRouterConfiguration().getSubnetRoutingTables().stream().filter(subnetRoutingTable -> subnetRoutingTable.getSubnetId().equals(subnetId)).findFirst().orElse(null);
                                Router.RouterState.Builder routerStateBuilder =  routerService.buildRouterState(networkConfiguration.getInternalRouterInfos().get(0), subnetRoutingTables);
                                multicastGoalState.getGoalStateBuilder().putRouterStates(routerId, routerStateBuilder.build());
                                multicastGoalState.getGoalStateBuilder().putSubnetStates(subnetId, subnetState);
                            }
                        } else {
                            multicastGoalState.getHostIps().add(portHostInfo.getHostIp());
                            neighborStateMap.put(neighborState.getConfiguration().getId(), neighborState);
                            if (!subnetStateMap.containsKey(subnetId)) {
                                Router.RouterConfiguration.SubnetRoutingTable.Builder subnetRoutingTableBuilder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
                                subnetRoutingTableBuilder.setSubnetId(internalSubnetPort.getSubnetId());
                                unicastRouterConfigurationBuilder.addSubnetRoutingTables(subnetRoutingTableBuilder.build());
                                subnetStateMap.put(internalSubnetPort.getSubnetId(), subnetService.buildSubnetState(internalSubnetPort.getSubnetId()).build());
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (CacheException e) {
                e.printStackTrace();
            }
        }

        subnetStateMap.put(subnetId, subnetState);
        for (UnicastGoalStateV2 unicastGoalStateV2 : unicastGoalStates.values()) {
            unicastGoalStateV2.getGoalStateBuilder().putAllNeighborStates(neighborStateMap);
            unicastGoalStateV2.getGoalStateBuilder().putAllSubnetStates(subnetStateMap);
            Router.RouterConfiguration.Builder routerConfigurationBuilder = multicastGoalState.getGoalStateBuilder().getRouterStatesMap().get(routerId).getConfiguration().toBuilder();
            routerConfigurationBuilder.addAllSubnetRoutingTables(unicastRouterConfigurationBuilder.getSubnetRoutingTablesList());
            Router.RouterState.Builder routerStateBuilder = Router.RouterState.newBuilder().setConfiguration(routerConfigurationBuilder.build());
            unicastGoalStateV2.getGoalStateBuilder().putRouterStates(routerId, routerStateBuilder.build());
        }
    }
}
