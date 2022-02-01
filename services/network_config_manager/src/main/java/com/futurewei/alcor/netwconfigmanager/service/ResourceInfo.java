package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.schema.Neighbor;

import java.util.Map;
import java.util.Set;

public interface ResourceInfo {
    Map<String, Neighbor.NeighborState> getNeighborStates (Set<String> resourceIds) throws Exception;
}
