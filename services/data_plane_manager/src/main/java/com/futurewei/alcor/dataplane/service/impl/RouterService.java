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

import com.futurewei.alcor.common.enumClass.VpcRouteTarget;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.schema.*;
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

    public void buildRouterState(InternalRouterInfo routerInfo, InternalSubnetRoutingTable subnetRoutingTable, UnicastGoalState unicastGoalState, MulticastGoalState multicastGoalState) {
        Router.RouterConfiguration.SubnetRoutingTable.Builder subnetRoutingTableBuilder = Router.RouterConfiguration.SubnetRoutingTable.newBuilder();
        String subnetId = subnetRoutingTable.getSubnetId();
        subnetRoutingTableBuilder.setSubnetId(subnetId);
        List<InternalRoutingRule> routingRules = subnetRoutingTable.getRoutingRules();

        if (routingRules != null && routingRules.size() > 0) {
            for (InternalRoutingRule routingRule: routingRules) {
                Router.RouterConfiguration.RoutingRule.Builder routingRuleBuilder = Router.RouterConfiguration.RoutingRule.newBuilder();
                routingRuleBuilder.setOperationType(getOperationType(routingRule.getOperationType()));
                routingRuleBuilder.setId(routingRule.getId());
                if (routingRule.getName() != null)
                {
                    routingRuleBuilder.setName(routingRule.getName());
                }
                routingRuleBuilder.setDestination(routingRule.getDestination());
                routingRuleBuilder.setNextHopIp(routingRule.getNextHopIp());
                routingRuleBuilder.setPriority(routingRule.getPriority());

                if (routingRule.getRoutingRuleExtraInfo() != null) {
                    Router.RouterConfiguration.RoutingRuleExtraInfo.Builder extraInfoBuilder = Router.RouterConfiguration.RoutingRuleExtraInfo.newBuilder();
                    extraInfoBuilder.setDestinationType(getDestinationType(
                            routingRule.getRoutingRuleExtraInfo().getDestinationType()));
                    if (routingRule.getRoutingRuleExtraInfo().getNextHopMac() != null)
                    {
                        extraInfoBuilder.setNextHopMac(routingRule.getRoutingRuleExtraInfo().getNextHopMac());
                    }
                    routingRuleBuilder.setRoutingRuleExtraInfo(extraInfoBuilder.build());
                }
                subnetRoutingTableBuilder.addRoutingRules(routingRuleBuilder.build());
            }
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
        routerConfigBuilder.setHostDvrMacAddress(HOST_DVR_MAC);
        if (routerInfo.getRouterConfiguration().getId() != null)
        {
            routerConfigBuilder.setId(routerInfo.getRouterConfiguration().getId());
        }
        routerConfigBuilder.addAllSubnetRoutingTables(subnetRoutingTablesList);
        Router.RouterState.Builder routerStateBuilder = Router.RouterState.newBuilder();
        routerStateBuilder.setConfiguration(routerConfigBuilder.build());
        goalStateBuilder.addRouterStates(routerStateBuilder.build());
        Goalstate.GoalState.Builder m_goalStateBuilder = multicastGoalState.getGoalStateBuilder();
        m_goalStateBuilder.addRouterStates(routerStateBuilder.build());
    }

    public void buildRouterStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState, MulticastGoalState multicastGoalState) throws Exception {
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
                        buildRouterState(routerInfo, subnetRoutingTable, unicastGoalState, multicastGoalState);
                        break;
                    }
                }
            }
        }
    }
}
