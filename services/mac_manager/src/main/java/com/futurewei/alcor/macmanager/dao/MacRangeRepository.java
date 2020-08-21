/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.macmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepositoryEx;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.mac.MacRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class MacRangeRepository implements ICacheRepositoryEx<MacRange> {
    private static final Logger logger = LoggerFactory.getLogger(MacRangeRepository.class);
    private ICache<String, MacRange> cache;

    @Autowired
    public MacRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(MacRange.class);
    }

    public ICache<String, MacRange> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.info("MacRangeRepository init: Done");
    }

    /**
     * get MAC range data
     *
     * @param rangeId MAC range id
     * @return MAC range
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public MacRange findItem(String rangeId) throws CacheException {
        MacRange macRange = null;
        try {
            macRange = cache.get(rangeId);
        } catch (CacheException e) {
            logger.error("MacRangeRepository findItem() exception:", e);
            throw e;
        }
        return macRange;
    }

    /**
     * get all MAC ranges
     *
     * @param
     * @return map of MAC ranges
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public Map<String, MacRange> findAllItems() throws CacheException {
        Map<String, MacRange> map = null;
        try {
            map = cache.getAll();
        } catch (CacheException e) {
            logger.error("MacRangeRepository findAllItems() exception:", e);
            throw e;
        }
        return map;
    }

    /**
     * get all MAC ranges by params filters
     *
     * @param queryParams url request params
     * @return map of MAC ranges
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public Map<String, MacRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        Map<String, MacRange> map = null;
        try {
            map = cache.getAll(queryParams);
        } catch (CacheException e) {
            logger.error("MacRangeRepository findAllItems() exception:", e);
            throw e;
        }
        return map;
    }

    /**
     * add a MAC range to MAC range repository
     *
     * @param macRange MAC range
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public void addItem(MacRange macRange) throws CacheException {
        cache.put(macRange.getRangeId(), macRange);
    }

    /**
     * delete a MAC range from MAC range repository
     *
     * @param rangeId MAC range identifier
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public void deleteItem(String rangeId) throws CacheException {
        cache.remove(rangeId);
    }

    @Override
    @DurationStatistics
    public long size() {
        return cache.size();
    }

    @Override
    @DurationStatistics
    public Boolean putIfAbsent(MacRange macRange) throws CacheException {
        return cache.putIfAbsent(macRange.getRangeId(), macRange);
    }

    @Override
    @DurationStatistics
    public Map<String, MacRange> findAllItems(Set<String> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    @DurationStatistics
    public Boolean contains(String key) throws CacheException {
        return cache.containsKey(key);
    }

    @Override
    public void addAllItem(Map<String, MacRange> newItems) throws CacheException {
        cache.putAll(newItems);
    }
}