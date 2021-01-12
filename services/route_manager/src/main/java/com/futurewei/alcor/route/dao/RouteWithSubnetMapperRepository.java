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

