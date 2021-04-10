/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    // The cache is a map(routerId, subnetIds)
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
