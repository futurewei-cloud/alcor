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
package com.futurewei.alcor.netwconfigmanager.util;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.exception.UnexpectedHostNumException;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Router;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

public class NetworkConfigManagerUtil {

    /**
     * split and build V2 GoalState in a host
     */
    public static Map<String, HostGoalState> splitClusterToHostGoalState(Goalstate.GoalStateV2 goalState) throws IllegalArgumentException {

        Map<String, HostGoalState> result = new HashMap<>();
        if (goalState == null || goalState.getHostResourcesCount() == 0) {
            return result;
        }

        Map<String, Goalstate.HostResources> hostResourcesMap = goalState.getHostResourcesMap();
        for (String hostId : hostResourcesMap.keySet()) {
            Goalstate.HostResources hostResourceMetadata = hostResourcesMap.get(hostId);
            HostGoalState hostGoalState = new HostGoalState();
            hostGoalState.getGoalStateBuilder().putHostResources(hostId, hostResourceMetadata);

            for (Goalstate.ResourceIdType resource : hostResourceMetadata.getResourcesList()) {
                String resourceId = resource.getId();
                switch (resource.getType()) {
                    case VPC:
                        hostGoalState.getGoalStateBuilder().putVpcStates(resourceId, goalState.getVpcStatesOrThrow(resourceId));
                        break;
                    case SUBNET:
                        hostGoalState.getGoalStateBuilder().putSubnetStates(resourceId, goalState.getSubnetStatesOrThrow(resourceId));
                        break;
                    case PORT:
                        hostGoalState.getGoalStateBuilder().putPortStates(resourceId, goalState.getPortStatesOrThrow(resourceId));
                        break;
                    case NEIGHBOR:
                        hostGoalState.getGoalStateBuilder().putNeighborStates(resourceId, goalState.getNeighborStatesOrThrow(resourceId));
                        break;
                    case SECURITYGROUP:
                        hostGoalState.getGoalStateBuilder().putSecurityGroupStates(resourceId, goalState.getSecurityGroupStatesOrThrow(resourceId));
                        break;
                    case DHCP:
                        hostGoalState.getGoalStateBuilder().putDhcpStates(resourceId, goalState.getDhcpStatesOrThrow(resourceId));
                        break;
                    case ROUTER:
                        Router.RouterState routerState = goalState.getRouterStatesOrThrow(resourceId);
                        String[] tokens = resourceId.split("/");
                        if (tokens.length == 2) {
                            resourceId = tokens[1];
                        }
                        hostGoalState.getGoalStateBuilder().putRouterStates(resourceId, routerState);
                        break;
                    case GATEWAY:
                        hostGoalState.getGoalStateBuilder().putGatewayStates(resourceId, goalState.getGatewayStatesOrThrow(resourceId));
                        break;
                    case UNRECOGNIZED:
                        break;
                }
            }

            hostGoalState.setHostIp(hostId);
            hostGoalState.completeGoalStateBuilder();
            result.put(hostId, hostGoalState);
        }

        return result;
    }

    public static Map<String, HostGoalState> filterNeighbors(Map<String, HostGoalState> hostGoalStates) throws UnexpectedHostNumException {
        Map<String, HostGoalState> filteredGoalStates = new HashMap<>();
        for (Map.Entry<String, HostGoalState> entry : hostGoalStates.entrySet()) {
            String hostId = entry.getKey();
            HostGoalState hostGoalState = entry.getValue();

            if (hostGoalState.getGoalState().getHostResourcesMap().size() != 1) throw new UnexpectedHostNumException();
            boolean filter = true;

            for (Goalstate.HostResources resources : hostGoalState.getGoalState().getHostResourcesMap().values()) {
                for (Goalstate.ResourceIdType resourceIdType : resources.getResourcesList()) {
                    if (resourceIdType.getType() == Common.ResourceType.PORT ||
                            resourceIdType.getType() == Common.ResourceType.DHCP ||
                            resourceIdType.getType() == Common.ResourceType.ROUTER ||
                            resourceIdType.getType() == Common.ResourceType.GATEWAY) {
                        filter = false;
                    }
                }
            }

            if (!filter) {
                filteredGoalStates.put(hostId, hostGoalState);
            }
        }

        return filteredGoalStates;
    }

    public static HostGoalState consolidateHostGoalState(HostGoalState existingState, HostGoalState newState) {
        //TODO: implement consolidation algorithm
        return newState;
    }

    public static ResourceMeta convertGoalStateToHostResourceMeta(String hostId, Goalstate.HostResources hostResourceMetadata) {

        ResourceMeta hostResourceMeta = new ResourceMeta(hostId);

        for (Goalstate.ResourceIdType resource : hostResourceMetadata.getResourcesList()) {
            String resourceId = resource.getId();
            switch (resource.getType()) {
                case VPC:
                    hostResourceMeta.addVpcId(resourceId);
                    break;
                case SUBNET:
                    hostResourceMeta.addSubnetId(resourceId);
                    break;
                case PORT:
                    hostResourceMeta.addPortId(resourceId);
                    break;
                case NEIGHBOR:
                    hostResourceMeta.addNeighborEntry(resourceId, resourceId); //TODO: where is neighbor ip
                    break;
                case SECURITYGROUP:
                    hostResourceMeta.addSecurityGroupId(resourceId);
                    break;
                case DHCP:
                    hostResourceMeta.addDhcpId(resourceId);
                    break;
                case ROUTER:
                    hostResourceMeta.addRouterId(resourceId);
                    break;
                case GATEWAY:
                    hostResourceMeta.addGatewayId(resourceId);
                    break;
                case UNRECOGNIZED:
                    break;
            }
        }

        return hostResourceMeta;
    }

    public static ResourceMeta consolidateResourceMeta(ResourceMeta existingState, ResourceMeta newState) {
        //TODO: implement consolidation algorithm
        //TODO: Consolidation is insufficient. We will need to calculate the resource differences to support delete
        return newState;
    }
}
