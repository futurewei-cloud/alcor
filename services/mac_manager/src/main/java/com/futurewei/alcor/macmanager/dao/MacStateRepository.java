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
import com.futurewei.alcor.web.entity.mac.MacState;
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
public class MacStateRepository implements ICacheRepositoryEx<MacState> {
    private static final Logger logger = LoggerFactory.getLogger(MacStateRepository.class);
    private ICache<String, MacState> cache;

    @Autowired
    public MacStateRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(MacState.class);
    }

    public ICache<String, MacState> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.info("MacState repository init: Done");
    }

    /**
     * get MAC-port allocation state
     *
     * @param macAddress MAC address
     * @return MAC state
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public MacState findItem(String macAddress) throws CacheException {
        return cache.get(macAddress);
    }

    /**
     * get all MAC-port allocation states
     *
     * @return map of all MAC states
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public Map<String, MacState> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, MacState> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    /**
     * add a new MAC-port allocation state to node repository
     *
     * @param macState MAC state
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public void addItem(MacState macState) throws CacheException {
        cache.put(macState.getMacAddress(), macState);
    }

    @Override
    @DurationStatistics
    public void addItems(List<MacState> items) throws CacheException {
        Map<String, MacState> macStateMap = items.stream().collect(Collectors.toMap(MacState::getMacAddress, Function.identity()));
        cache.putAll(macStateMap);
    }

    /**
     * delete a MAC-port allocation state from node repository
     *
     * @param macAddress MAC address
     * @throws CacheException Db or cache operation exception
     */
    @Override
    @DurationStatistics
    public void deleteItem(String macAddress) throws CacheException {
        cache.remove(macAddress);
    }

    @Override
    @DurationStatistics
    public long size() {
        return cache.size();
    }

    @Override
    @DurationStatistics
    public Boolean putIfAbsent(MacState macState) throws CacheException {
        return cache.putIfAbsent(macState.getMacAddress(), macState);
    }

    @Override
    @DurationStatistics
    public Map<String, MacState> findAllItems(Set<String> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    @DurationStatistics
    public Boolean contains(String key) throws CacheException {
        return cache.containsKey(key);
    }

    @Override
    public void addAllItem(Map<String, MacState> newItems) throws CacheException {
        cache.putAll(newItems);
    }
}
