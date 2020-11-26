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

import com.futurewei.alcor.common.enumClass.VpcRouteTarget;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Router;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalRoutingRule;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RouterService extends ResourceService {
    @Autowired
    private SubnetService subnetService;

    private Router.DestinationType getDestinationType(VpcRouteTarget vpcRouteTarget) {
        switch (vpcRouteTarget) {
            case LOCAL:
                return Router.DestinationType.INTERNET;
            case INTERNET_GW:
                return Router.DestinationType.VPC_GW;
            case NAT_GW:
            default:
                return Router.DestinationType.UNRECOGNIZED;
        }
    }

    public void buildRouterState(InternalRouterInfo routerInfo, InternalSubnetRoutingTable subnetRoutingTable, UnicastGoalState unicastGoalState) { Router.RouterConfiguration.SubnetRoutingTable.Builder subnetRoutingTableBuilder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
        String subnetId = subnetRoutingTable.getSubnetId();
        subnetRoutingTableBuilder.setSubnetId(subnetId);
        List<InternalRoutingRule> routingRules = subnetRoutingTable.getRoutingRules();
        if (routingRules == null || routingRules.size() == 0) {
            return;
        }

        for (InternalRoutingRule routingRule: routingRules) {
            Router.RouterConfiguration.RoutingRule.Builder routingRuleBuilder = Router.RouterConfiguration.RoutingRule.newBuilder();
            routingRuleBuilder.setOperationType(getOperationType(routingRule.getOperationType()));
            routingRuleBuilder.setId(routingRule.getId());
            routingRuleBuilder.setName(routingRule.getName());
            routingRuleBuilder.setDestination(routingRule.getDestination());
            routingRuleBuilder.setNextHopIp(routingRule.getNextHopIp());
            routingRuleBuilder.setPriority(Integer.parseInt(routingRule.getPriority()));

            if (routingRule.getRoutingRuleExtraInfo() != null) {
                Router.RouterConfiguration.RoutingRuleExtraInfo.Builder extraInfoBuilder = Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder();
                extraInfoBuilder.setDestinationType(getDestinationType(
                        routingRule.getRoutingRuleExtraInfo().getDestinationType()));
                extraInfoBuilder.setNextHopMac(routingRule.getRoutingRuleExtraInfo().getNextHopMac());
                routingRuleBuilder.setRoutingRuleExtraInfo(extraInfoBuilder.build());
            }

            subnetRoutingTableBuilder.addRoutingRules(routingRuleBuilder.build());
        }

        List<Router.RouterConfiguration.SubnetRoutingTable> subnetRoutingTablesList = new ArrayList<>();
        subnetRoutingTablesList.add(subnetRoutingTableBuilder.build());

        Goalstate.GoalState.Builder goalStateBuilder = unicastGoalState.getGoalStateBuilder();
        List<Router.RouterState.Builder> routerStatesBuilders = goalStateBuilder.getRouterStatesBuilderList();
        if (routerStatesBuilders != null && routerStatesBuilders.size() > 0) {
            subnetRoutingTablesList.addAll(goalStateBuilder.
                    getRouterStatesBuilder(0).
                    getConfiguration().
                    getSubnetRoutingTablesList());
            goalStateBuilder.removeRouterStates(0);
        }

        Router.RouterConfiguration.Builder routerConfigBuilder = Router.RouterConfiguration.newBuilder();
        routerConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);

        //TODO: where does the hostDvrMacAddress come from ?
        routerConfigBuilder.setHostDvrMacAddress(routerInfo.getRouterConfiguration().getHostDvrMac());

        routerConfigBuilder.addAllSubnetRoutingTables(subnetRoutingTablesList);
        Router.RouterState.Builder routerStateBuilder = Router.RouterState.newBuilder();
        routerStateBuilder.setConfiguration(routerConfigBuilder.build());
        goalStateBuilder.addRouterStates(routerStateBuilder.build());
    }

    public void buildRouterStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        Set<String> subnetIds = new HashSet<>();
        for (Port.PortState portState: portStates) {
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                InternalSubnetEntity internalSubnetEntity =
                        subnetService.getInternalSubnetEntity(networkConfig, fixedIp.getSubnetId());
                subnetIds.add(internalSubnetEntity.getId());
            }
        }

        List<InternalRouterInfo> internalRouterInfos = networkConfig.getInternalRouterInfos();
        if (internalRouterInfos == null || internalRouterInfos.size() == 0) {
            return;
        }

        for (String subnetId: subnetIds) {
            for (InternalRouterInfo routerInfo : internalRouterInfos) {
                List<InternalSubnetRoutingTable> subnetRoutingTables =
                        routerInfo.getRouterConfiguration().getSubnetRoutingTables();
                if (subnetRoutingTables == null) {
                    continue;
                }

                for (InternalSubnetRoutingTable subnetRoutingTable : subnetRoutingTables) {
                    if (subnetId.equals(subnetRoutingTable.getSubnetId())) {
                        buildRouterState(routerInfo, subnetRoutingTable, unicastGoalState);
                        break;
                    }
                }
            }
        }
    }
}
