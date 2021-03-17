package com.futurewei.alcor.netwconfigmanager.util;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.exception.UnexpectedHostNumException;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkConfigManagerUtil {

    /**
     * split and build V2 GoalState in a host
     */
    public static Map<String, HostGoalState> splitClusterToHostGoalState(Goalstate.GoalStateV2 goalState) {

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

    public static void filterNeighbors(Map<String, HostGoalState> hostGoalStates) throws UnexpectedHostNumException {
        for (Map.Entry<String, HostGoalState> entry : hostGoalStates.entrySet()) {
            String hostId = entry.getKey();
            HostGoalState hostGoalState = entry.getValue();

            if (hostGoalState.getGoalState().getHostResourcesMap().size() != 1) throw new UnexpectedHostNumException();
            Boolean filter = true;

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

            if (filter) {
                hostGoalStates.remove(hostId);
            }
        }
    }
}
