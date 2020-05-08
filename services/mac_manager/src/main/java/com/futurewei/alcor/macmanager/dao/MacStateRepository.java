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
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.macmanager.entity.MacState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class MacStateRepository implements ICacheRepository<MacState> {
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

    @Override
    public Map findAllItems() throws CacheException {
        HashMap hashMap = new HashMap();
        try {
            hashMap = new HashMap(cache.getAll());
        } catch (CacheException e) {
            logger.error("MacStateRepository findAllItems() exception:", e);
            throw e;
        }
        return hashMap;
    }

    @Override
    public void addItem(MacState macState) throws CacheException {
        try {
            cache.put(macState.getMacAddress(), macState);
            logger.info("MacStateRepository addItem() {}: ", macState.getMacAddress());
        } catch (CacheException e) {
            logger.error("MacStateRepository addItem() exception:", e);
            throw e;
        }
    }

    @Override
    public void deleteItem(String macAddress) throws CacheException {
        try {
            cache.remove(macAddress);
            logger.info("MacStateRepository deleteItem() {}: ", macAddress);
        } catch (CacheException e) {
            logger.error("MacStateRepository deleteItem() exception:", e);
            throw e;
        }
    }
}
