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
package com.futurewei.alcor.dataplane.service.ovs;

import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.*;
import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.service.DpmService;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DpmServiceImpl implements DpmService {
    private int goalStateMessageVersion;

    @Autowired
    private LocalCache localCache;

    @Autowired
    private DataPlaneClient dataPlaneClient;

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
                                                   MulticastGoalState multicastGoalState) throws Exception {
        UnicastGoalState unicastGoalState = new UnicastGoalState();
        unicastGoalState.setHostIp(hostIp);

        unicastGoalState.getGoalStateBuilder().setFormatVersion(this.goalStateMessageVersion);

        if (portEntities != null && portEntities.size() > 0) {
            portService.buildPortState(networkConfig, portEntities, unicastGoalState);
        }

        vpcService.buildVpcStates(networkConfig, unicastGoalState);
        subnetService.buildSubnetStates(networkConfig, unicastGoalState);
        neighborService.buildNeighborStates(networkConfig, hostIp, unicastGoalState, multicastGoalState);
        securityGroupService.buildSecurityGroupStates(networkConfig, unicastGoalState);
        dhcpService.buildDhcpStates(networkConfig, unicastGoalState);
        routerService.buildRouterStates(networkConfig, unicastGoalState);

        unicastGoalState.setGoalState(unicastGoalState.getGoalStateBuilder().build());
        unicastGoalState.setGoalStateBuilder(null);

        return unicastGoalState;
    }

    private List<String> createPortConfiguration(NetworkConfiguration networkConfig) throws Exception {
        List<UnicastGoalState> unicastGoalStates = new ArrayList<>();
        MulticastGoalState multicastGoalState = new MulticastGoalState();

        Map<String, List<InternalPortEntity>> hostPortEntities = new HashMap<>();
        for (InternalPortEntity portEntity: networkConfig.getPortEntities()) {
            if (portEntity.getBindingHostIP() == null) {
                throw new PortBindingHostIpNotFound();
            }

            if (!hostPortEntities.containsKey(portEntity.getBindingHostIP())) {
                hostPortEntities.put(portEntity.getBindingHostIP(), new ArrayList<>());
            }

            hostPortEntities.get(portEntity.getBindingHostIP()).add(portEntity);
        }

        for (Map.Entry<String, List<InternalPortEntity>> entry: hostPortEntities.entrySet()) {
            String hostIp = entry.getKey();
            List<InternalPortEntity> portEntities = entry.getValue();
            unicastGoalStates.add(buildUnicastGoalState(
                    networkConfig, hostIp, portEntities, multicastGoalState));
        }

        localCache.addSubnetPorts(networkConfig);

        multicastGoalState.setGoalState(multicastGoalState.getGoalStateBuilder().build());
        multicastGoalState.setGoalStateBuilder(null);

        return dataPlaneClient.createGoalStates(unicastGoalStates, multicastGoalState);
    }

    private List<String> createNeighborConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    private List<String> createSecurityGroupConfiguration(NetworkConfiguration networkConfig) throws Exception {
        return null;
    }

    private List<String> createRouterConfiguration(NetworkConfiguration networkConfig) throws Exception {
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
        return dataPlaneClient.createGoalStates(unicastGoalStates);
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

    @Override
    public InternalDPMResultList createNetworkConfiguration(NetworkConfiguration networkConfig) throws Exception {
        long startTime = System.currentTimeMillis();
        List<String> failedHosts;

        switch (networkConfig.getRsType()) {
            case PORT:
                failedHosts = createPortConfiguration(networkConfig);
                break;
            case NEIGHBOR:
                failedHosts = createNeighborConfiguration(networkConfig);
                break;
            case SECURITYGROUP:
                failedHosts = createSecurityGroupConfiguration(networkConfig);
                break;
            case ROUTER:
                failedHosts = createRouterConfiguration(networkConfig);
                break;
            default:
                throw new UnknownResourceType();
        }

        return buildResult(networkConfig, failedHosts, startTime);
    }

    @Override
    public InternalDPMResultList updateNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception {
        return null;
    }

    @Override
    public InternalDPMResultList deleteNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception {
        return null;
    }
}