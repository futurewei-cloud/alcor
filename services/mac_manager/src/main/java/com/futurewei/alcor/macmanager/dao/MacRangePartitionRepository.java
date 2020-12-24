/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.macmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.mac.MacRangePartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class MacRangePartitionRepository implements ICacheRepository<MacRangePartition> {

    private final ICache<String, MacRangePartition> cache;

    @Autowired
    public MacRangePartitionRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(MacRangePartition.class);
    }

    @Override
    @DurationStatistics
    public MacRangePartition findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, MacRangePartition> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, MacRangePartition> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(MacRangePartition macRangePartition) throws CacheException {
        cache.put(macRangePartition.getId(), macRangePartition);
    }

    @Override
    @DurationStatistics
    public void addItems(List<MacRangePartition> items) throws CacheException {
        Map<String, MacRangePartition> macRangePartitionMap = items.stream().collect(Collectors.toMap(MacRangePartition::getId, Function.identity()));
        cache.putAll(macRangePartitionMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
