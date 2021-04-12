package com.futurewei.alcor.netwconfigmanager.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.entity.VpcResourceMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class VpcResourceCache {

    // Map <VNI, Map<PIP, List<ResoruceIDType>>
    private ICache<String, VpcResourceMeta> vpcResourceMetas;

    @Autowired
    public VpcResourceCache(CacheFactory cacheFactory) {
        this.vpcResourceMetas = cacheFactory.getCache(VpcResourceMeta.class);
    }

    @DurationStatistics
    public VpcResourceMeta getResourceMeta(String vni) throws Exception {
        VpcResourceMeta resourceMeta = this.vpcResourceMetas.get(vni);

        return resourceMeta;
    }

    @DurationStatistics
    public void addResourceMeta(VpcResourceMeta resourceMeta) throws Exception {
        this.vpcResourceMetas.put(resourceMeta.getVni(), resourceMeta);
    }

    @DurationStatistics
    public void updateResourceMeta(VpcResourceMeta resourceMeta) throws Exception {
        this.vpcResourceMetas.put(resourceMeta.getVni(), resourceMeta);
    }

    @DurationStatistics
    public void deleteResourceMeta(String vni) throws Exception {
        this.vpcResourceMetas.remove(vni);
    }
}
