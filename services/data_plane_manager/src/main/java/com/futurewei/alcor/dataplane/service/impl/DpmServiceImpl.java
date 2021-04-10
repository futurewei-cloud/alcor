/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.VpcGatewayInfoCache;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.client.ZetaGatewayClient;
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
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DpmServiceImpl implements DpmService {
    private static final Logger LOG = LoggerFactory.getLogger(DpmServiceImpl.class);
    private static final boolean USE_PULSAR_CLIENT = false;

    private int goalStateMessageVersion;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    @Value("${zetaGateway.enabled:false}")
    private boolean zetaGatwayEnabled;

    @Autowired
    private ZetaGatewayClient zetaGatewayClient;

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
    private ZetaPortService zetaPortService;

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
                                                   MulticastGoalState multicastGoalState) throws Exception {
        UnicastGoalState unicastGoalState = new UnicastGoalState();
        unicastGoalState.setHostIp(hostIp);

        unicastGoalState.getGoalStateBuilder().setFormatVersion(this.goalStateMessageVersion);

        if (portEntities != null && portEntities.size() > 0) {
            portService.buildPortState(networkConfig, portEntities, unicastGoalState);
        }

        vpcService.buildVpcStates(networkConfig, unicastGoalState);
        subnetService.buildSubnetStates(networkConfig, unicastGoalState, multicastGoalState);
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

    private List<String> doCreatePortConfiguration(NetworkConfiguration networkConfig,
                                                   Map<String, List<InternalPortEntity>> hostPortEntities,
                                                   DataPlaneClient dataPlaneClient) throws Exception {
        List<UnicastGoalState> unicastGoalStates = new ArrayList<>();
        MulticastGoalState multicastGoalState = new MulticastGoalState();

        ZetaPortGoalState zetaPortsGoalState = new ZetaPortGoalState();
        List<String> failedZetaPorts = new ArrayList<>();

        for (Map.Entry<String, List<InternalPortEntity>> entry : hostPortEntities.entrySet()) {
            String hostIp = entry.getKey();
            List<InternalPortEntity> portEntities = entry.getValue();

            if (zetaGatwayEnabled) {
                if (portEntities != null && portEntities.size() > 0) {
                    zetaPortService.buildPortState(networkConfig, portEntities, zetaPortsGoalState);
                }
            }

            unicastGoalStates.add(buildUnicastGoalState(
                    networkConfig, hostIp, portEntities, multicastGoalState));
        }
        // portEntities in the same unicastGoalStates should have the same opType

        if (zetaGatwayEnabled && zetaPortsGoalState.getPortEntities().size() > 0) {
            return zetaGatewayClient.sendGoalStateToZetaAcA(unicastGoalStates, multicastGoalState, dataPlaneClient, zetaPortsGoalState, failedZetaPorts);
        } else {
            return dataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
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
    private List<String> processPortConfiguration(NetworkConfiguration networkConfig) throws Exception {
        Map<String, List<InternalPortEntity>> grpcHostPortEntities = new HashMap<>();
        Map<String, List<InternalPortEntity>> pulsarHostPortEntities = new HashMap<>();

        for (InternalPortEntity portEntity : networkConfig.getPortEntities()) {
            if (portEntity.getBindingHostIP() == null) {
                throw new PortBindingHostIpNotFound();
            }

            if (zetaGatwayEnabled) {
                zetaGatewayClient.enableZetaGatewayForPort(portEntity);
            }

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
                    networkConfig, grpcHostPortEntities, grpcDataPlaneClient));
        }

        if (pulsarHostPortEntities.size() != 0) {
            statusList.addAll(doCreatePortConfiguration(
                    networkConfig, pulsarHostPortEntities, pulsarDataPlaneClient));
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
        if (failedHosts == null || failedHosts.size() == 0) {
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

        switch (networkConfig.getRsType()) {
            case PORT:
                failedHosts = processPortConfiguration(networkConfig);
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