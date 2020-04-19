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

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.ICache;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.common.service.CacheFactory;
import com.futurewei.alcor.macmanager.entity.MacState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(CacheFactory.class)
public class MacRepository implements ICacheRepository<MacState> {
    private static final Logger logger = LoggerFactory.getLogger();
    private ICache<String, MacState> cache;

    @Autowired
    public MacRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(MacState.class);
    }

    public ICache<String, MacState> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "MacRepository init completed");
    }

    @Override
    public MacState findItem(String macAddress) throws CacheException {
        return cache.get(macAddress);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(MacState macState) throws CacheException {
        logger.log(Level.INFO, "Add mac state, mac_state:" + macState.getMacAddress());
        cache.put(macState.getMacAddress(), macState);
    }

    @Override
    public void deleteItem(String macAddress) throws CacheException {
        logger.log(Level.INFO, "Delete mac state, mac address:" + macAddress);
        cache.remove(macAddress);
    }
}