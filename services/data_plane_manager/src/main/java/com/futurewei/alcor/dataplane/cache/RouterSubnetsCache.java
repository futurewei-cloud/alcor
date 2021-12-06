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
import com.futurewei.alcor.dataplane.entity.InternalSubnetRouterMap;
import com.futurewei.alcor.dataplane.entity.InternalSubnets;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class RouterSubnetsCache {
    // The cache is a map(routerId, subnetIds)
    private final ICache<String, InternalSubnetRouterMap> routerSubnetsCache;
    private CacheFactory cacheFactory;

    @Autowired
    public RouterSubnetsCache(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.routerSubnetsCache = cacheFactory.getCache(InternalSubnetRouterMap.class);
    }

    @DurationStatistics
    public synchronized Collection<InternalSubnetRouterMap> getRouterSubnets(String routerId) throws CacheException {
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = routerId;
        queryParams.put("routerId", values);
        return routerSubnetsCache.getAll(queryParams).values();

    }

    @DurationStatistics
    public synchronized Collection<InternalSubnetRouterMap> updateVpcSubnets(NetworkConfiguration networkConfig) throws CacheException {
        Map<String, InternalSubnetRouterMap> internalSubnetsMap =  networkConfig
                .getInternalRouterInfos()
                .stream()
                .filter(routerInfo -> routerInfo.getRouterConfiguration().getSubnetRoutingTables().size() > 0)
                .flatMap(routerInfo -> routerInfo.getRouterConfiguration().getSubnetRoutingTables()
                        .stream()
                        .map(routingTable -> new InternalSubnetRouterMap(routerInfo.getRouterConfiguration().getId()
                                , routingTable.getSubnetId())))
                .collect(Collectors.toMap(routerInfo -> routerInfo.getRouterId() + cacheFactory.KEY_DELIMITER + routerInfo.getSubnetId(), Function.identity()));
        routerSubnetsCache.putAll(internalSubnetsMap);
        return internalSubnetsMap.values();
    }

    @DurationStatistics
    public synchronized void deleteVpcGatewayInfo(String routerId, String subnetId) throws CacheException {
        routerSubnetsCache.remove(routerId + cacheFactory.KEY_DELIMITER + subnetId);
    }


}
