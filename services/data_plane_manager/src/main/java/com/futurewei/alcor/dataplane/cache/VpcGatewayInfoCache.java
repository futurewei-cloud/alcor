/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class VpcGatewayInfoCache implements ICacheRepository<GatewayInfo> {
    private final ICache<String, GatewayInfo> vpcGatewayInfoCache;

    @Autowired
    public VpcGatewayInfoCache(CacheFactory cacheFactory) {
        this.vpcGatewayInfoCache = cacheFactory.getCache(GatewayInfo.class);
    }

    @DurationStatistics
    public GatewayInfo getVpcGatewayInfo(String resourceId) throws CacheException {
        return vpcGatewayInfoCache.get(resourceId);
    }

    @DurationStatistics
    public synchronized void addVpcGatewayInfo(GatewayInfo gatewayInfo) throws CacheException {
        vpcGatewayInfoCache.put(gatewayInfo.getResourceId(), gatewayInfo);
    }

    @DurationStatistics
    public void updateVpcGatewayInfo(GatewayInfo gatewayInfo) throws CacheException {
        vpcGatewayInfoCache.put(gatewayInfo.getResourceId(), gatewayInfo);
    }

    @DurationStatistics
    public void deleteVpcGatewayInfo(String resourceId) throws CacheException {
        vpcGatewayInfoCache.remove(resourceId);
    }

    @Override
    public GatewayInfo findItem(String id) throws CacheException {
        return vpcGatewayInfoCache.get(id);
    }

    @Override
    public Map<String, GatewayInfo> findAllItems() throws CacheException {
        return vpcGatewayInfoCache.getAll();
    }

    @Override
    public Map<String, GatewayInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return vpcGatewayInfoCache.getAll(queryParams);
    }

    @Override
    public void addItem(GatewayInfo gatewayInfo) throws CacheException {
        log.debug("Add GatewayInfo, gatewayInfo : {}", gatewayInfo);
        vpcGatewayInfoCache.put(gatewayInfo.getResourceId(), gatewayInfo);
    }

    public void addItems(List<GatewayInfo> items) throws CacheException {
        Map<String, GatewayInfo> gatewayInfoMap = items.stream().collect(Collectors.toMap(GatewayInfo::getResourceId, Function.identity()));
        vpcGatewayInfoCache.putAll(gatewayInfoMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete GatewayInfo, GatewayInfo resource_id is: {}", id);
        vpcGatewayInfoCache.remove(id);
    }
}
