package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class HostResourceMetadataCache {

    // Map <HostId, List<ResoruceIDType>>
    private ICache<String, ResourceMeta> hostResourceMetas;

    @Autowired
    public HostResourceMetadataCache(CacheFactory cacheFactory) {
        this.hostResourceMetas = cacheFactory.getCache(ResourceMeta.class);
    }

    @DurationStatistics
    public ResourceMeta getResourceMeta(String hostId) throws Exception {
        ResourceMeta resourceState = this.hostResourceMetas.get(hostId);

        return resourceState;
    }

    @DurationStatistics
    public void addResourceMeta(ResourceMeta resourceMeta) throws Exception {
        this.hostResourceMetas.put(resourceMeta.getOwnerId(), resourceMeta);
    }

    @DurationStatistics
    public void updateResourceMeta(ResourceMeta resourceMeta) throws Exception {
        this.hostResourceMetas.put(resourceMeta.getOwnerId(), resourceMeta);
    }

    @DurationStatistics
    public void deleteResourceMeta(String hostId) throws Exception {
        this.hostResourceMetas.remove(hostId);
    }
}
