/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    @DurationStatistics
    public void addItems(List<MacRange> items) throws CacheException {
        Map<String, MacRange> macRangeMap = items.stream().collect(Collectors.toMap(MacRange::getRangeId, Function.identity()));
        cache.putAll(macRangeMap);
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