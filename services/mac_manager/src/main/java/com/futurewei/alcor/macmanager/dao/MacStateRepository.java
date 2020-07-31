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
import com.futurewei.alcor.web.entity.mac.MacState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

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
    public MacState findItem(String macAddress) throws CacheException {
        MacState macState = null;
        try {
            macState = cache.get(macAddress);
        } catch (CacheException e) {
            logger.error("MacStateRepository findItem() exception:", e);
            throw e;
        }
        return macState;
    }

    /**
     * get all MAC-port allocation states
     *
     * @param
     * @return map of all MAC states
     * @throws CacheException Db or cache operation exception
     */
    @Override
    public Map<String, MacState> findAllItems() throws CacheException {
        Map<String, MacState> map = null;
        try {
            map = cache.getAll();
        } catch (CacheException e) {
            logger.error("MacStateRepository findAllItems() exception:", e);
            throw e;
        }
        return map;
    }

    @Override
    public Map<String, MacState> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        Map<String, MacState> map = null;
        try {
            map = cache.getAll(queryParams);
        } catch (CacheException e) {
            logger.error("MacStateRepository findAllItems() exception:", e);
            throw e;
        }
        return map;
    }

    /**
     * add a new MAC-port allocation state to node repository
     *
     * @param macState MAC state
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void addItem(MacState macState) throws CacheException {
        cache.put(macState.getMacAddress(), macState);
    }

    /**
     * delete a MAC-port allocation state from node repository
     *
     * @param macAddress MAC address
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void deleteItem(String macAddress) throws CacheException {
        cache.remove(macAddress);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Boolean putIfAbsent(MacState macState) throws CacheException {
        return cache.putIfAbsent(macState.getMacAddress(), macState);
    }

    @Override
    public Map<String, MacState> findAllItems(Set<String> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    public Boolean contains(String key) throws CacheException {
        return cache.containsKey(key);
    }
}
