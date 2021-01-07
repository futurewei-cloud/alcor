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
