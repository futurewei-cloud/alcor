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
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.web.entity.port.PortState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@ComponentScan(value="com.futurewei.alcor.common.db")
@Repository
public class PortRepository implements ICacheRepository<PortState> {
    private static final Logger LOG = LoggerFactory.getLogger(PortRepository.class);

    private ICache<String, PortState> cache;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(PortState.class);
    }

    @PostConstruct
    private void init() {
        LOG.info("PortRepository init done");
    }

    @Override
    public PortState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, PortState> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(PortState portState) throws CacheException {
        cache.put(portState.getId(), portState);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
