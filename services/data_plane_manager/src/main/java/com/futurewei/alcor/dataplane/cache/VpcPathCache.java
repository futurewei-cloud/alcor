package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class VpcPathCache {
    // This cache is a map (vpcId, current chosen path). True for GRPC, and false for MQ.
    private static boolean USE_GRPC = true;

    private ICache<String, Boolean> vpcPathCache;

    @Autowired
    public VpcPathCache(CacheFactory cacheFactory) {
        vpcPathCache = cacheFactory.getCache(Boolean.class);
    }

    @DurationStatistics
    public boolean getCurrentPathByVpcId(String vpcId) throws Exception{
        return vpcPathCache.get(vpcId);
    }

    @DurationStatistics
    public void setPath(String vpcId, boolean path) throws Exception {
        vpcPathCache.put(vpcId, path);
    }
}
