/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
    // The cache is a map(vpcId, gatewayInfo)
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
