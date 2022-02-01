package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.netwconfigmanager.cache.ResourceStateCache;
import com.futurewei.alcor.netwconfigmanager.service.ResourceInfo;
import com.futurewei.alcor.schema.Neighbor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.cache")
public class ResourceInfoImpl implements ResourceInfo {
    @Autowired
    private ResourceStateCache resourceStateCache;

    public Map<String, Neighbor.NeighborState> getNeighborStates (Set<String> resourceIds) throws Exception {
        return resourceStateCache.getResourceStates(resourceIds);
    }
}
