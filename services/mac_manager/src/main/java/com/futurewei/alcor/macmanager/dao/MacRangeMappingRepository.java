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
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.macmanager.dao.api.IRangeMappingRepository;
import com.futurewei.alcor.web.entity.mac.MacAllocate;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.w3c.dom.ranges.Range;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MacRangeMappingRepository implements IRangeMappingRepository {

    private final Map<String, ICache<Long, String>> mappingCache;

    private final CacheFactory cacheFactory;

    @Autowired
    public MacRangeMappingRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        mappingCache = new ConcurrentHashMap<>();
    }

    @Override
    @DurationStatistics
    public long getUsedCapacity(String rangeId) throws CacheException {
        return getRangeCache(rangeId).size();
    }

    @Override
    @DurationStatistics
    public Boolean putIfAbsent(String rangeId, Long macLong) throws CacheException {
        return getRangeCache(rangeId).putIfAbsent(macLong, rangeId);
    }

    @Override
    @DurationStatistics
    public void addItem(String rangeId, Long macLong) throws CacheException {
        getRangeCache(rangeId).put(macLong, rangeId);
    }

    @Override
    @DurationStatistics
    public Boolean releaseMac(String rangeId, Long macLong) throws CacheException {
        return getRangeCache(rangeId).remove(macLong);
    }

    @Override
    @DurationStatistics
    public void removeRange(String rangeId) throws CacheException {
        //TODO clear cache
        mappingCache.remove(rangeId);
    }

    @Override
    @DurationStatistics
    public Set<Long> getAll(String rangeId, Set<Long> macs) throws CacheException{
        Map<Long, String> existMacs =getRangeCache(rangeId).getAll(macs);
        Set<Long> newMacs = new HashSet<>();
        for(Long mac : macs){
            if(!existMacs.containsKey(mac) || existMacs.get(mac) == null){
                newMacs.add(mac);
            }
        }
        return newMacs;
    }

    @Override
    public void putAll(String rangeId, Map<Long, String> entries) throws CacheException {
        getRangeCache(rangeId).putAll(entries);
    }

    private ICache<Long, String> getRangeCache(String rangeId){
        ICache<Long, String> cache = mappingCache.get(rangeId);
        if (cache == null) {
            mappingCache.putIfAbsent(rangeId, cacheFactory.getCache(String.class, rangeId));
            cache = mappingCache.get(rangeId);
        }
        return cache;
    }
}
