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

package com.futurewei.alcor.subnet.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.web.entity.subnet.SubnetWebObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SubnetRepository implements ICacheRepository<SubnetWebObject> {

    private static final Logger logger = LoggerFactory.getLogger();

    private static final String KEY = "SubnetState";

    public ICache<String, SubnetWebObject> getCache() {
        return cache;
    }

    private ICache<String, SubnetWebObject> cache;

    @Autowired
    public SubnetRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(SubnetWebObject.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SubnetRepository init completed");
    }

    @Override
    public SubnetWebObject findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, SubnetWebObject> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(SubnetWebObject routeState) throws CacheException {
        logger.log(Level.INFO, "Add subnet, subnet Id:" + routeState.getId());
        cache.put(routeState.getId(), routeState);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete subnet, subnet Id:" + id);
        cache.remove(id);
    }
}
