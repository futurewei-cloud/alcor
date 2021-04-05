package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class HostResourceStateCache {
    // Map <ResourceId, ResourceState>
    private ICache<String, Object> hostResourceStates;

    @Autowired
    public HostResourceStateCache(CacheFactory cacheFactory) {
        this.hostResourceStates = cacheFactory.getCache(Object.class);
    }

    @DurationStatistics
    public Object getResourceState(String resourceId) throws Exception {
        Object resourceState = this.hostResourceStates.get(resourceId);

        return resourceState;
    }

    @DurationStatistics
    public void addResourceState(String resourceId, Object resourceState) throws Exception {
        this.hostResourceStates.put(resourceId, resourceState);
    }

    @DurationStatistics
    public void updateResourceState(String resourceId, Object resourceState) throws Exception {
        this.hostResourceStates.put(resourceId, resourceState);
    }

    @DurationStatistics
    public void deleteResourceState(String resourceId) throws Exception {
        this.hostResourceStates.remove(resourceId);
    }
}
