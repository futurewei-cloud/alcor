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
package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.route.VpcToRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class RouteWithVpcMapperRepository implements ICacheRepository<VpcToRouteMapper> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, VpcToRouteMapper> getCache() {
        return cache;
    }

    private ICache<String, VpcToRouteMapper> cache;

    @Autowired
    public RouteWithVpcMapperRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(VpcToRouteMapper.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteWithVpcMapperRepository init completed");
    }

    @Override
    @DurationStatistics
    public VpcToRouteMapper findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, VpcToRouteMapper> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, VpcToRouteMapper> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(VpcToRouteMapper vpcToRouteMapper) throws CacheException {
        logger.log(Level.INFO, "Add RouteWithVpcMapper, mapper Id:" + vpcToRouteMapper.getVpcId());
        cache.put(vpcToRouteMapper.getVpcId(), vpcToRouteMapper);
    }

    @Override
    @DurationStatistics
    public void addItems(List<VpcToRouteMapper> items) throws CacheException {
        logger.log(Level.INFO, "Add RouteWithVpcMapper Batch: {}",items);
        Map<String, VpcToRouteMapper> vpcToRouteMapperMap = items.stream().collect(Collectors.toMap(VpcToRouteMapper::getVpcId, Function.identity()));
        cache.putAll(vpcToRouteMapperMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete RouteWithVpcMapper, mapper Id:" + id);
        cache.remove(id);
    }
}
