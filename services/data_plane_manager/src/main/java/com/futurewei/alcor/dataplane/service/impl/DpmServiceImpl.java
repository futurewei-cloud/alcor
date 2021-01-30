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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.StatusEnum;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.VpcGatewayInfoCache;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.ZetaPortGoalState;
import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.dataplane.service.DpmService;
import com.futurewei.alcor.schema.Neighbor;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry.NeighborType;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.gateway.*;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class DpmServiceImpl implements DpmService {
    private static final Logger LOG = LoggerFactory.getLogger(DpmServiceImpl.class);
    private static final boolean USE_PULSAR_CLIENT = false;

    @Value("${zetaGateway.check.timeout}")
    private String zetaGatewayCheckTimeout;

    @Value("${zetaGateway.check.interval}")
    private String zetaGatewayCheckInterval;

    @Value("${microservices.gateway.service.url}")
    private String gatewayUrl;

    private RestTemplate restTemplate = new RestTemplate();

    private int goalStateMessageVersion;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    @Autowired
    private LocalCache localCache;

    @Autowired
    private VpcGatewayInfoCache gatewayInfoCache;

    @Autowired
    private DataPlaneClient grpcDataPlaneClient;

    @Autowired
    private DataPlaneClient pulsarDataPlaneClient;;

    @Autowired
    private VpcService vpcService;

    @Autowired
    private SubnetService subnetService;

    @Autowired
    private PortService portService;

    @Autowired
    private NeighborService neighborService;

    @Autowired
    private SecurityGroupService securityGroupService;

    @Autowired
    private DhcpService dhcpService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private DpmServiceImpl(Config globalConfig) {
        this.goalStateMessageVersion = globalConfig.goalStateMessageVersion;
    }

    private UnicastGoalState buildUnicastGoalState(NetworkConfiguration networkConfig, String hostIp,
                                                   List<InternalPortEntity> portEntities,
                                                   MulticastGoalState multicastGoalState,
                                                   ZetaPortGoalState zetaPortGoalState) throws Exception {
        UnicastGoalState unicastGoalState = new UnicastGoalState();
        unicastGoalState.setHostIp(hostIp);

        unicastGoalState.getGoalStateBuilder().setFormatVersion(this.goalStateMessageVersion);

        if (portEntities != null && portEntities.size() > 0) {
            portService.buildPortState(networkConfig, portEntities, unicastGoalState, zetaPortGoalState);
        }

        vpcService.buildVpcStates(networkConfig, unicastGoalState);
        subnetService.buildSubnetStates(networkConfig, unicastGoalState);
        neighborService.buildNeighborStates(networkConfig, hostIp, unicastGoalState, multicastGoalState);
        securityGroupService.buildSecurityGroupStates(networkConfig, unicastGoalState);
        dhcpService.buildDhcpStates(networkConfig, unicastGoalState);
        routerService.buildRouterStates(networkConfig, unicastGoalState);

        unicastGoalState.setGoalState(unicastGoalState.getGoalStateBuilder().build());
        unicastGoalState.setGoalStateBuilder(null);
        multicastGoalState.setGoalState(multicastGoalState.getGoalStateBuilder().build());
        multicastGoalState.setGoalStateBuilder(null);

        return unicastGoalState;
    }

    @SuppressWarnings("unchecked")
    private List<String> sendGoalStates(Object arg1, Object arg2, Object arg3) throws Exception {
        DataPlaneClient dataPlaneClient = (DataPlaneClient) arg1;
        List<UnicastGoalState> unicastGoalStates = (List<UnicastGoalState>) arg2;
        MulticastGoalState multicastGoalState = (MulticastGoalState) arg3;
        return dataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
    }

    private ZetaPortsWebJson zetaSendGoalState(Object arg1) throws Exception {
        ZetaPortGoalState zetaPortGoalState = (ZetaPortGoalState) arg1;
        ZetaPortsWebJson zetaPortsWebJson = new ZetaPortsWebJson();
//        if (zetaPortGoalState.getPortEntities().size() > 0) {
//
//            if (Common.OperationType.CREATE.equals(zetaPortGoalState.getOpType())) {
//                zetaPortCreate.add(zetaPortGoalState.getPortEntity());
//            } else if (Common.OperationType.DELETE.equals(zetaPortGoalState.getOpType())) {
//                zetaPortDelete.add(zetaPortGoalState.getPortEntity());
//            } else if (Common.OperationType.UPDATE.equals(zetaPortGoalState.getOpType())) {
//                zetaPortUpdate.add(zetaPortGoalState.getPortEntity());
//            }
//        }
        return zetaPortsWebJson;
    }

    private List<String> asyncRun1(Object arg1, Object arg2) throws Exception {
        List<String> failed = new ArrayList<>();
        failed.add("host1");
        failed.add("host2");
//        try {
//            throw new NullPointerException("demo");
//        } catch (NullPointerException e) {
//            System.out.println("Caught inside fun().");
//            throw e; // rethrowing the exception
//        }
        return failed;
    }

    @SuppressWarnings("unchecked")
    private ZetaPortsWebJson asyncRun2(Object arg1, Object arg2) throws Exception {
        List<ZetaPortEntity> zeta = (List<ZetaPortEntity>) arg1;
        ZetaPortsWebJson z = new ZetaPortsWebJson();
        if (zeta.size() > 0) {
            ArrayList<ZetaPortEntity> listofz = new ArrayList<>(zeta.size());
            listofz.addAll(zeta);
            z.setZetaPorts(listofz);
        }
        return z;
    }

//    private ResponseId asyncRun3(Object arg1, Object arg2) throws Exception {
//        String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
//        String vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88041";
//        List<GatewayEntity> gatewayEntities = new ArrayList<>();
//        gatewayEntities.add(new GatewayEntity(null, null, null, null,
//                GatewayType.ZETA, StatusEnum.READY.getStatus(),
//                null, null, null, null, null, null));
//        GatewayInfo gatewayInfo = new GatewayInfo(vpcId, gatewayEntities, null, null);
//        String url = gatewayUrl + "/gatewayinfo";
//        HttpEntity<GatewayInfoJson> vpcHttpEntity = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
//        return restTemplate.postForObject(url, vpcHttpEntity, ResponseId.class);
//    }

//    private void rollbackZetaAca(List<Object> results) {
//        if (results != null && results.size() < 2) {
//
//        } else if (results != null) {
//            List<ZetaPortEntity> zetaports = new ArrayList<>();
//            GatewayInfoJson gatewayInfoJson = new GatewayInfoJson();
//            for (Object result : results) {
//                if (result instanceof List<?>) {
//                    for (Object obj : (List<?>) result) {
//                        if (obj instanceof String) {
//                            failedHosts.add((String) obj);
//                        }
//                    }
//                } else if (result instanceof ZetaPortsWebJson) {
//                    zetaports.addAll(((ZetaPortsWebJson) result).getZetaPorts());
//                }
//            }
//            List<ZetaPortEntity> req_zeta = new ArrayList<>(zetaPortEntities);
//            for (String host: failedHosts) {
//                for (ZetaPortEntity zeta: zetaports) {
//                    // zetaport ok, but aca failed, rollback zeta
//                    if (zeta.getNodeIp().equals(host)) {
//                        // rollback zeta
//                        break;
//                    }
//                }
//                fHosts.remove(host);
//            }
//            if (fHosts.size() > 0) {
//                // rollback ACA
//            }
//        }
//    }

    private List<String> sendGoalStateToZetaAcA(List<UnicastGoalState> unicastGoalStates,
                                                 MulticastGoalState multicastGoalState,
                                                 DataPlaneClient dataPlaneClient,
                                                 ZetaPortGoalState zetaPortGoalState,
                                                 List<String> failedZetaPorts) throws Exception {
        List<String> failedHosts = new ArrayList<>();
        AsyncExecutor executor = new AsyncExecutor();
        executor.runAsync(this::sendGoalStates, dataPlaneClient, unicastGoalStates, multicastGoalState);
        executor.runAsync(this::zetaSendGoalState, zetaPortGoalState);
        executor.runAsync(this::asyncRun1, null, null);
        executor.runAsync(this::asyncRun2, zetaPortGoalState, null);
        //executor.runAsync(this::asyncRun3, null, null);
        List<Object> results = null;
        try {
            results = executor.joinAll();
            //rollbackZetaAca(results);
        } catch (Exception e) {
            LOG.error("", e);
            executor.waitAll();
            //rollbackZetaAca(results);
            throw e;
        }

        return failedHosts;
    }

    private List<String> doCreatePortConfiguration(NetworkConfiguration networkConfig,
                                                   Map<String, List<InternalPortEntity>> hostPortEntities,
                                                   DataPlaneClient dataPlaneClient,
                                                   List<String> failedZetaPorts) throws Exception {
        List<UnicastGoalState> unicastGoalStates = new ArrayList<>();
        MulticastGoalState multicastGoalState = new MulticastGoalState();
        ZetaPortGoalState zetaPortsGoalState = new ZetaPortGoalState();

        for (Map.Entry<String, List<InternalPortEntity>> entry : hostPortEntities.entrySet()) {
            String hostIp = entry.getKey();
            List<InternalPortEntity> portEntities = entry.getValue();
            unicastGoalStates.add(buildUnicastGoalState(
                    networkConfig, hostIp, portEntities, multicastGoalState, zetaPortsGoalState));
        }
        // portEntities in the same unicastGoalStates should have the same opType

        if (zetaPortsGoalState.getPortEntities().size() > 0) {
            return sendGoalStateToZetaAcA(unicastGoalStates, multicastGoalState, dataPlaneClient, zetaPortsGoalState, failedZetaPorts);
        } else {
            return dataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
        }
    }

    private void updateVPCZetaGateway(GatewayInfo gatewayInfo) throws Exception {
        String url = gatewayUrl + "project/" + "/gatewayinfo/" + gatewayInfo.getResourceId();
        HttpEntity<GatewayInfoJson> gatewayHttpEntity = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
        restTemplate.put(url, gatewayHttpEntity, ResponseId.class);
    }

    private void checkZetaGateway(InternalPortEntity portEntity) throws Exception {
        GatewayInfo gatewayInfo = gatewayInfoCache.findItem(portEntity.getVpcId());
        if (gatewayInfo == null) {
            List<GatewayEntity> newGatewayEntities = new ArrayList<>();
            newGatewayEntities.add(new GatewayEntity(null, null, null, null, GatewayType.ZETA,
                    StatusEnum.NOTAVAILABLE.getStatus(), null, null, null, null, null, null));
            GatewayInfo newGatewayInfo = new GatewayInfo(portEntity.getVpcId(), newGatewayEntities, null, "available");
            gatewayInfoCache.addItem(newGatewayInfo);

            // notify GM to update VPC’s zeta gateway status
            updateVPCZetaGateway(newGatewayInfo);
            portEntity.setIsZetaGatewayPort(Boolean.FALSE);
        } else {
            for (GatewayEntity gateway : gatewayInfo.getGatewayEntities()) {
                if (GatewayType.ZETA.equals(gateway.getType())) {
                    if (StatusEnum.READY.getStatus().equals(gateway.getStatus())) {
                        portEntity.setIsZetaGatewayPort(Boolean.TRUE);
                    } else if (StatusEnum.PENDING.getStatus().equals(gateway.getStatus())) {
                        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                        Future<?> future = executor.scheduleAtFixedRate(() -> {
                            try {
                                GatewayInfo gwInfo = gatewayInfoCache.findItem(portEntity.getVpcId());
                                for (GatewayEntity gw: gwInfo.getGatewayEntities()) {
                                    if (GatewayType.ZETA.equals(gw.getType())) {
                                        if (StatusEnum.READY.getStatus().equals(gw.getStatus())) {
                                            portEntity.setIsZetaGatewayPort(Boolean.TRUE);
                                            executor.shutdown();
                                        }
                                        break;
                                    }
                                }
                            } catch (CacheException e) {
                                e.printStackTrace();
                            }
                        }, 0, Long.parseLong(zetaGatewayCheckInterval), TimeUnit.SECONDS);

                        try {
                            future.get(Long.parseLong(zetaGatewayCheckTimeout), TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            portEntity.setIsZetaGatewayPort(Boolean.FALSE);
                            gateway.setStatus(StatusEnum.FAILED.getStatus());
                            e.printStackTrace();
                            executor.shutdown();
                        }
                    } else {
                        gateway.setStatus(StatusEnum.FAILED.getStatus());
                        // something wrong for the zeta gateway, raise alarm?
                    }
                    break;
                }
            }
        }
    }

    /**
     * This method processes the network configuration where the resource type is PORT.
     * The NetworkConfiguration may contain multiple port entities, and these port
     * entities may need to be sent to multiple hosts, Other configurations related to port
     * also need to be processed, such as routing configuration and neighbor configuration.
     * Here we build one UnicastGoalState for each host and put the information that it needs
     * into it. If the same information needs to be sent to multiple host, in order to
     * improve the transmission performance, only one MulticastGoalState is constructed and
     * broadcast it to the all target  hosts.
     * @param networkConfig The networkConfig may contain multiple port entities and related Configuration
     * @return Hosts that failed to send GoalState
     * @throws Exception Process exceptions and send exceptions
     */
    private List<String> processPortConfiguration(NetworkConfiguration networkConfig, List<String> failedZetaPorts) throws Exception {
        Map<String, List<InternalPortEntity>> grpcHostPortEntities = new HashMap<>();
        Map<String, List<InternalPortEntity>> pulsarHostPortEntities = new HashMap<>();

        for (InternalPortEntity portEntity : networkConfig.getPortEntities()) {
            if (portEntity.getBindingHostIP() == null) {
                throw new PortBindingHostIpNotFound();
            }

            //if (OperationType.CREATE.getOperationType().equals(networkConfig.getOpType().toString())) {
            checkZetaGateway(portEntity);
            //}

            boolean fastPath = portEntity.getFastPath() == null ? false : portEntity.getFastPath();
            if (fastPath) {
                if (!grpcHostPortEntities.containsKey(portEntity.getBindingHostIP())) {
                    grpcHostPortEntities.put(portEntity.getBindingHostIP(), new ArrayList<>());
                }
                grpcHostPortEntities.get(portEntity.getBindingHostIP()).add(portEntity);
            } else {
                if (!pulsarHostPortEntities.containsKey(portEntity.getBindingHostIP())) {
                    pulsarHostPortEntities.put(portEntity.getBindingHostIP(), new ArrayList<>());
                }
                pulsarHostPortEntities.get(portEntity.getBindingHostIP()).add(portEntity);
            }
        }

        List<String> statusList = new ArrayList<>();

        if (grpcHostPortEntities.size() != 0) {
            statusList.addAll(doCreatePortConfiguration(
                    networkConfig, grpcHostPortEntities, grpcDataPlaneClient, failedZetaPorts));
        }

        if (pulsarHostPortEntities.size() != 0) {
            statusList.addAll(doCreatePortConfiguration(
                    networkConfig, pulsarHostPortEntities, pulsarDataPlaneClient, failedZetaPorts));
        }

        localCache.updateLocalCache(networkConfig);

        return statusList;
    }

    /**
     * This method get neighbor information from NetworkConfiguration, then build one
     * UnicastGoalState for each host and fill in the neighbor information it needs,
     * and finally send all UnicastGoalState to the relevant hosts.
     * @param networkConfig Network configuration with neighbor configuration
     * @return Hosts that failed to send GoalState
     * @throws Exception Process exceptions and send exceptions
     */
    private List<String> processNeighborConfiguration(NetworkConfiguration networkConfig) throws Exception {
        Map<String, NeighborInfo> neighborInfos = networkConfig.getNeighborInfos();
        Map<String, List<NeighborEntry>> neighborTable = networkConfig.getNeighborTable();
        List<UnicastGoalState> unicastGoalStates = new ArrayList<>();
        MulticastGoalState multicastGoalState = new MulticastGoalState();

        if (neighborTable == null || neighborInfos == null) {
            throw new NeighborInfoNotFound();
        }

        Map<String, List<NeighborInfo>> hostNeighbors = new HashMap<>();
        for (Map.Entry<String, List<NeighborEntry>> entry: neighborTable.entrySet()) {
            String portIp = entry.getKey();
            NeighborInfo localInfo = neighborInfos.get(portIp);
            if (localInfo == null) {
                throw new NeighborInfoNotFound();
            }

            String hostIp = localInfo.getHostIp();
            if (!hostNeighbors.containsKey(hostIp)) {
                hostNeighbors.put(hostIp, new ArrayList<>());
            }

            List<NeighborEntry> neighborEntries = entry.getValue();
            for (NeighborEntry neighborEntry: neighborEntries) {
                String neighborIp = neighborEntry.getNeighborIp();
                NeighborInfo neighborInfo = neighborInfos.get(neighborIp);
                if (neighborInfo == null) {
                    throw new NeighborInfoNotFound();
                }

                hostNeighbors.get(hostIp).add(neighborInfo);
                multicastGoalState.getHostIps().add(neighborInfo.getHostIp());
            }

            //Add neighborInfo to multicastGoalState
            Neighbor.NeighborState neighborState = neighborService.buildNeighborState(
                    NeighborType.L3, localInfo, networkConfig.getOpType());
            multicastGoalState.getGoalStateBuilder().addNeighborStates(neighborState);
        }

        for (Map.Entry<String, List<NeighborInfo>> entry: hostNeighbors.entrySet()) {
            String hostIp = entry.getKey();
            List<NeighborInfo> hostNeighborInfos = entry.getValue();

            /**
             * At present, there are only L3 neighbors in the neighbor table,
             * and the processing of L2 neighbors should be considered in the future.
             */
            for (NeighborInfo neighborInfo: hostNeighborInfos) {
                Neighbor.NeighborState neighborState = neighborService.buildNeighborState(
                        NeighborType.L3,
                        neighborInfo,
                        networkConfig.getOpType());

                UnicastGoalState unicastGoalState = new UnicastGoalState();
                unicastGoalState.setHostIp(neighborInfo.getHostIp());
                unicastGoalState.getGoalStateBuilder().addNeighborStates(neighborState);
                unicastGoalStates.add(unicastGoalState);
            }
        }

        multicastGoalState.setGoalState(multicastGoalState.getGoalStateBuilder().build());
        multicastGoalState.setGoalStateBuilder(null);
        unicastGoalStates.stream().forEach(u -> {
            u.setGoalState(u.getGoalStateBuilder().build());
            u.setGoalStateBuilder(null);
        });

        if (USE_PULSAR_CLIENT) {
            return pulsarDataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
        }

        return grpcDataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
    }

    private List<String> processSecurityGroupConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    /**
     * This method get the subnet routing configuration from the NetworkConfiguration,
     * then find the host information of all the virtual machines under the subnet from
     * the local cache according to the subnet id, build one UnicastGoalState for each
     * host and fill it with the routing information it needs, and finally send all the
     * UnicastGoalState to the relevant hosts.
     * @param networkConfig Network configuration with router configuration
     * @return Hosts that failed to send GoalState
     * @throws Exception Process exceptions and send exceptions
     */
    private List<String> processRouterConfiguration(NetworkConfiguration networkConfig) throws Exception {
        List<InternalRouterInfo> internalRouterInfos = networkConfig.getInternalRouterInfos();
        if (internalRouterInfos == null) {
            throw new RouterInfoInvalid();
        }

        Map<String, UnicastGoalState> unicastGoalStateMap = new HashMap<>();
        for (InternalRouterInfo routerInfo: internalRouterInfos) {
            List<InternalSubnetRoutingTable> subnetRoutingTables =
                    routerInfo.getRouterConfiguration().getSubnetRoutingTables();
            if (subnetRoutingTables == null) {
                throw new RouterInfoInvalid();
            }

            for (InternalSubnetRoutingTable subnetRoutingTable: subnetRoutingTables) {
                String subnetId = subnetRoutingTable.getSubnetId();
                InternalSubnetPorts subnetPorts = localCache.getSubnetPorts(subnetId);
                if (subnetPorts == null) {
                    throw new SubnetPortsNotFound();
                }

                for (PortHostInfo portHostInfo: subnetPorts.getPorts()) {
                    String hostIp = portHostInfo.getHostIp();
                    UnicastGoalState unicastGoalState = unicastGoalStateMap.get(hostIp);
                    if (unicastGoalState == null) {
                        unicastGoalState = new UnicastGoalState();
                        unicastGoalState.setHostIp(hostIp);
                        unicastGoalStateMap.put(hostIp, unicastGoalState);
                    }

                    routerService.buildRouterState(routerInfo, subnetRoutingTable, unicastGoalState);
                }
            }
        }

        if (unicastGoalStateMap.size() == 0) {
            throw new RouterInfoInvalid();
        }

        List<UnicastGoalState> unicastGoalStates = unicastGoalStateMap.values()
                .stream().peek(gs -> {
                    gs.setGoalState(gs.getGoalStateBuilder().build());
                    gs.setGoalStateBuilder(null);
                }).collect(Collectors.toList());

        //TODO: Merge UnicastGoalState with the same content, build MulticastGoalState
        return grpcDataPlaneClient.sendGoalStates(unicastGoalStates);
    }

    private InternalDPMResultList buildResult(NetworkConfiguration networkConfig, List<String> failedHosts, long startTime) {
        InternalDPMResultList result = new InternalDPMResultList();
        long endTime = System.currentTimeMillis();
        result.setOverrallTime(endTime - startTime);

        String resultMessage;
        if (failedHosts.size() == 0) {
            resultMessage = "Successfully Handle request !!";
        } else {
            resultMessage = "Failed Handle request !!";
        }

        InternalDPMResult internalDPMResult = new InternalDPMResult();
        internalDPMResult.setElapseTime(endTime - startTime);
        internalDPMResult.setFailedHosts(failedHosts);
        //internalDPMResult.setResourceId(networkConfig.getRsType());
        internalDPMResult.setResourceType(networkConfig.getRsType());
        internalDPMResult.setStatus(resultMessage);

        result.setResultMessage(resultMessage);
        result.setResultList(Collections.singletonList(internalDPMResult));

        return result;
    }

    private InternalDPMResultList processNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        long startTime = System.currentTimeMillis();
        List<String> failedHosts;
        List<String> failedZetaPorts = new ArrayList<>();

        switch (networkConfig.getRsType()) {
            case PORT:
                failedHosts = processPortConfiguration(networkConfig, failedZetaPorts);
                break;
            case NEIGHBOR:
                failedHosts = processNeighborConfiguration(networkConfig);
                break;
            case SECURITYGROUP:
                failedHosts = processSecurityGroupConfiguration(networkConfig);
                break;
            case ROUTER:
                failedHosts = processRouterConfiguration(networkConfig);
                break;
            default:
                throw new UnknownResourceType();
        }

        return buildResult(networkConfig, failedHosts, startTime);
    }

    @Override
    public InternalDPMResultList createNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return processNetworkConfiguration(networkConfig);
    }

    @Override
    public InternalDPMResultList updateNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return processNetworkConfiguration(networkConfig);
    }

    @Override
    public InternalDPMResultList deleteNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return processNetworkConfiguration(networkConfig);
    }
}