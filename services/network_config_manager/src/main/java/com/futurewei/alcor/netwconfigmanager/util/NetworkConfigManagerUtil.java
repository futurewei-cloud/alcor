package com.futurewei.alcor.netwconfigmanager.util;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.schema.Goalstate;

import java.util.HashMap;
import java.util.Map;

public class NetworkConfigManagerUtil {

    /**
     * split and build V2 GoalState in a host
     *
     */
    public static Map<String, HostGoalState> splitClusterToHostGoalState(Goalstate.GoalStateV2 goalState){

        Map<String, HostGoalState> result = new HashMap<>();
        if (goalState == null || goalState.getHostResourcesCount() == 0) {
            return result;
        }

        Map<String, Goalstate.HostResources> hostResourcesMap = goalState.getHostResourcesMap();
        for (String hostId : hostResourcesMap.keySet()) {
            Goalstate.HostResources hostResourceMetadata = hostResourcesMap.get(hostId);
            HostGoalState hostGoalState = new HostGoalState();

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
                        hostGoalState.getGoalStateBuilder().putRouterStates(resourceId, goalState.getRouterStatesOrThrow(resourceId));
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
}
