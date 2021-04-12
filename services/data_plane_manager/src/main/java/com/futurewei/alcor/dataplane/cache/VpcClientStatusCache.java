package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.config.ClientConstant;
import com.futurewei.alcor.dataplane.exception.InvalidChannelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class VpcClientStatusCache {
    private ICache<String, String> vpcClientStatusCache;

    @Autowired
    public VpcClientStatusCache(CacheFactory cacheFactory) {
        vpcClientStatusCache = cacheFactory.getCache(String.class);
    }

    @DurationStatistics
    public void addVpcClientStatus(String vpcId, String clientStatus) throws Exception {
        if (!ClientConstant.clientChoices.contains(clientStatus)) {
            throw new InvalidChannelException("Invalid channel choose.");
        }
        vpcClientStatusCache.put(vpcId, clientStatus);
    }

    @DurationStatistics
    public String getClientStatusByVpcId(String vpcId) throws Exception {
        return vpcClientStatusCache.get(vpcId);
    }

    @DurationStatistics
    public void updateVpcClientStatus(String vpcId, String clientStatus) throws Exception {
        if (!ClientConstant.clientChoices.contains(clientStatus)) {
            throw new InvalidChannelException("Invalid channel choose.");
        }
        vpcClientStatusCache.put(vpcId, clientStatus);
    }
}