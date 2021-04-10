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

package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.route.SubnetToRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class RouteWithSubnetMapperRepository implements ICacheRepository<SubnetToRouteMapper> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, SubnetToRouteMapper> getCache() {
        return cache;
    }

    private ICache<String, SubnetToRouteMapper> cache;

    @Autowired
    public RouteWithSubnetMapperRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(SubnetToRouteMapper.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteWithSubnetMapperRepository init completed");
    }

    @Override
    @DurationStatistics
    public SubnetToRouteMapper findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetToRouteMapper> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetToRouteMapper> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(SubnetToRouteMapper subnetToRouteMapper) throws CacheException {
        logger.log(Level.INFO, "Add routeWithSubnetMapper, mapper Id:" + subnetToRouteMapper.getSubnetId());
        cache.put(subnetToRouteMapper.getSubnetId(), subnetToRouteMapper);
    }

    @Override
    @DurationStatistics
    public void addItems(List<SubnetToRouteMapper> items) throws CacheException {
        logger.log(Level.INFO, "Add routeWithSubnetMapper Batch: {}",items);
        Map<String, SubnetToRouteMapper> subnetToRouteMapperMap = items.stream().collect(Collectors.toMap(SubnetToRouteMapper::getSubnetId, Function.identity()));
        cache.putAll(subnetToRouteMapperMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete routeWithSubnetMapper, mapper Id:" + id);
        cache.remove(id);
    }
}

