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
import com.futurewei.alcor.web.entity.route.RouteWithSubnetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class RouteWithSubnetMapperRepository implements ICacheRepository<RouteWithSubnetMapper> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouteWithSubnetMapper> getCache() {
        return cache;
    }

    private ICache<String, RouteWithSubnetMapper> cache;

    @Autowired
    public RouteWithSubnetMapperRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouteWithSubnetMapper.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteWithSubnetMapperRepository init completed");
    }

    @Override
    public RouteWithSubnetMapper findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, RouteWithSubnetMapper> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(RouteWithSubnetMapper routeWithSubnetMapper) throws CacheException {
        logger.log(Level.INFO, "Add routeWithSubnetMapper, mapper Id:" + routeWithSubnetMapper.getSubnetId());
        cache.put(routeWithSubnetMapper.getSubnetId(), routeWithSubnetMapper);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete routeWithSubnetMapper, mapper Id:" + id);
        cache.remove(id);
    }
}

