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

package com.futurewei.alcor.dataplane.dao.repo;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.repo.ICache;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.common.service.CacheFactory;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

import static com.futurewei.alcor.schema.Port.PortState;
@Repository
@ConditionalOnBean(CacheFactory.class)
public class PortRepository implements ICacheRepository<Port.PortState> {
    private static final Logger logger = LoggerFactory.getLogger();

    private ICache<String, Port.PortState> cache;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(Port.PortState.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "PortRepository init completed");
    }

    @Override
    public Port.PortState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }


    @Override
    public void addItem(PortState newItem) throws CacheException {
        logger.log(Level.INFO, "Port Id:" + newItem.getConfiguration().getId());
        cache.put(newItem.getConfiguration().getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
