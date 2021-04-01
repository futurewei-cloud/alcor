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
public class VpcSubnetsCache implements ICacheRepository<InternalSubnets> {
    // The cache is a map(vpcId, subnetIds)
    private final ICache<String, InternalSubnets> vpcSubnetsCache;

    @Autowired
    public VpcSubnetsCache(CacheFactory cacheFactory) {
        this.vpcSubnetsCache = cacheFactory.getCache(InternalSubnets.class);
    }

    @DurationStatistics
    public InternalSubnets getVpcSubnets(String vpcId) throws CacheException {
        return vpcSubnetsCache.get(vpcId);
    }

    @DurationStatistics
    public Map<String, InternalSubnets> getAllSubnets() throws CacheException {
        return vpcSubnetsCache.getAll();
    }

    @DurationStatistics
    public Map<String, InternalSubnets> getAllSubnets(Map<String, Object[]> queryParams) throws CacheException {
        return vpcSubnetsCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void addVpcSubnets(InternalSubnets subnets) throws CacheException {
        vpcSubnetsCache.put(subnets.getVpcId(), subnets);
    }

    @DurationStatistics
    public void updateVpcSubnets(InternalSubnets subnets) throws CacheException {
        vpcSubnetsCache.put(subnets.getVpcId(), subnets);
    }

    @DurationStatistics
    public void deleteVpcGatewayInfo(String vpcId) throws CacheException {
        vpcSubnetsCache.remove(vpcId);
    }

    @Override
    public InternalSubnets findItem(String id) throws CacheException {
        return vpcSubnetsCache.get(id);
    }

    @Override
    public Map<String, InternalSubnets> findAllItems() throws CacheException {
        return vpcSubnetsCache.getAll();
    }

    @Override
    public Map<String, InternalSubnets> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return vpcSubnetsCache.getAll(queryParams);
    }

    @Override
    public void addItem(InternalSubnets subnets) throws CacheException {
        log.debug("Add Subnets {} to VPC {}", subnets.toString(), subnets.getVpcId());
        vpcSubnetsCache.put(subnets.getVpcId(), subnets);
    }

    @Override
    public void addItems(List<InternalSubnets> items) throws CacheException {
        Map<String, InternalSubnets> subnetsMap = items.stream().collect(Collectors.toMap(InternalSubnets::getVpcId, Function.identity()));
        vpcSubnetsCache.putAll(subnetsMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete VPC {}", id);
        vpcSubnetsCache.remove(id);
    }
}
