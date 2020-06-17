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
import com.futurewei.alcor.web.entity.route.RouteWithVpcMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class RouteWithVpcMapperRepository implements ICacheRepository<RouteWithVpcMapper> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouteWithVpcMapper> getCache() {
        return cache;
    }

    private ICache<String, RouteWithVpcMapper> cache;

    @Autowired
    public RouteWithVpcMapperRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouteWithVpcMapper.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteWithVpcMapperRepository init completed");
    }

    @Override
    public RouteWithVpcMapper findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, RouteWithVpcMapper> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(RouteWithVpcMapper routeWithVpcMapper) throws CacheException {
        logger.log(Level.INFO, "Add RouteWithVpcMapper, mapper Id:" + routeWithVpcMapper.getVpcId());
        cache.put(routeWithVpcMapper.getVpcId(), routeWithVpcMapper);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete RouteWithVpcMapper, mapper Id:" + id);
        cache.remove(id);
    }
}
