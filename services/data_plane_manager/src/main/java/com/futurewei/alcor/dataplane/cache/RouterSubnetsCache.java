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
import com.futurewei.alcor.dataplane.entity.InternalSubnets;
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
public class RouterSubnetsCache implements ICacheRepository<InternalSubnets> {
    private final ICache<String, InternalSubnets> routerSubnetsCache;

    @Autowired
    public RouterSubnetsCache(CacheFactory cacheFactory) {
        this.routerSubnetsCache = cacheFactory.getCache(InternalSubnets.class);
    }

    @DurationStatistics
    public InternalSubnets getRouterSubnets(String routerId) throws CacheException {
        return routerSubnetsCache.get(routerId);
    }

    @DurationStatistics
    public Map<String, InternalSubnets> getAllSubnets() throws CacheException {
        return routerSubnetsCache.getAll();
    }

    @DurationStatistics
    public Map<String, InternalSubnets> getAllSubnets(Map<String, Object[]> queryParams) throws CacheException {
        return routerSubnetsCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void addVpcSubnets(InternalSubnets subnets) throws CacheException {
        routerSubnetsCache.put(subnets.getRouterId(), subnets);
    }

    @DurationStatistics
    public void updateVpcSubnets(InternalSubnets subnets) throws CacheException {
        routerSubnetsCache.put(subnets.getRouterId(), subnets);
    }

    @DurationStatistics
    public void deleteVpcGatewayInfo(String routerId) throws CacheException {
        routerSubnetsCache.remove(routerId);
    }

    @Override
    public InternalSubnets findItem(String id) throws CacheException {
        return routerSubnetsCache.get(id);
    }

    @Override
    public Map<String, InternalSubnets> findAllItems() throws CacheException {
        return routerSubnetsCache.getAll();
    }

    @Override
    public Map<String, InternalSubnets> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return routerSubnetsCache.getAll(queryParams);
    }

    @Override
    public void addItem(InternalSubnets subnets) throws CacheException {
        log.debug("Add Subnets {} to Router {}", subnets.toString(), subnets.getRouterId());
        routerSubnetsCache.put(subnets.getRouterId(), subnets);
    }

    @Override
    public void addItems(List<InternalSubnets> items) throws CacheException {
        Map<String, InternalSubnets> subnetsMap = items.stream().collect(Collectors.toMap(InternalSubnets::getRouterId, Function.identity()));
        routerSubnetsCache.putAll(subnetsMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete Router {}", id);
        routerSubnetsCache.remove(id);
    }
}
