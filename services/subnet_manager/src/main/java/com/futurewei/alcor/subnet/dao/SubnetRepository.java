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
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class SubnetRepository implements ICacheRepository<SubnetEntity> {

    private static final Logger logger = LoggerFactory.getLogger();

    private static final String KEY = "SubnetState";

    public ICache<String, SubnetEntity> getCache() {
        return cache;
    }

    private ICache<String, SubnetEntity> cache;

    @Autowired
    public SubnetRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(SubnetEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SubnetRepository init completed");
    }

    @Override
    @DurationStatistics
    public SubnetEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(SubnetEntity subnet) throws CacheException {
        logger.log(Level.INFO, "Add subnet, subnet Id:" + subnet.getId());
        cache.put(subnet.getId(), subnet);
    }

    @Override
    @DurationStatistics
    public void addItems(List<SubnetEntity> items) throws CacheException {
        logger.log(Level.INFO, "Add subnet batch: {}",items);
        Map<String, SubnetEntity> subnetEntityMap = items.stream().collect(Collectors.toMap(SubnetEntity::getId, Function.identity()));
        cache.putAll(subnetEntityMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete subnet, subnet Id:" + id);
        cache.remove(id);
    }
}
