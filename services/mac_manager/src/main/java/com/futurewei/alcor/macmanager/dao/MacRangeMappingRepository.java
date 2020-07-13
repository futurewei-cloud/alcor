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
import com.futurewei.alcor.macmanager.dao.api.IRangeMappingRepository;
import com.futurewei.alcor.web.entity.mac.MacAllocate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.w3c.dom.ranges.Range;

import java.util.HashMap;
import java.util.Map;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class MacRangeMappingRepository implements IRangeMappingRepository {

    private final Map<String, ICache<String, MacAllocate>> mappingCache;

    private final CacheFactory cacheFactory;

    @Autowired
    public MacRangeMappingRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        mappingCache = new HashMap<>();
    }

    @Override
    public long size(String rangeId) throws CacheException {
        if(!mappingCache.containsKey(rangeId)){
            createRangeCache(rangeId);
        }
        return mappingCache.get(rangeId).size();
    }

    @Override
    public Boolean putIfAbsent(String rangeId, MacAllocate macAllocate) throws CacheException {
        if(!mappingCache.containsKey(rangeId)){
            createRangeCache(rangeId);
        }
        return mappingCache.get(rangeId).putIfAbsent(macAllocate.getMac(), macAllocate);
    }

    @Override
    public void addItem(String rangeId, MacAllocate newItem) throws CacheException {
        mappingCache.get(rangeId).put(newItem.getMac(), newItem);
    }

    @Override
    public Boolean releaseMac(String rangeId, String macAddress) throws CacheException {
        if(!mappingCache.containsKey(rangeId)){
            createRangeCache(rangeId);
        }
        return mappingCache.get(rangeId).remove(macAddress);
    }

    private void createRangeCache(String rangeId){
        mappingCache.put(rangeId, cacheFactory.getCache(MacAllocate.class, rangeId));
    }

    @Override
    public void removeRange(String rangeId) throws CacheException {
        //TODO clear cache
        mappingCache.remove(rangeId);
    }
}
