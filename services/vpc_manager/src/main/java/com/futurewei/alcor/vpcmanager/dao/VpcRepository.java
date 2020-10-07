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

package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class VpcRepository implements ICacheRepository<VpcEntity> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, VpcEntity> getCache() {
        return cache;
    }

    private ICache<String, VpcEntity> cache;

    @Autowired
    public VpcRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(VpcEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "VpcRepository init completed");
    }

    @Override
    @DurationStatistics
    public VpcEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, VpcEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(VpcEntity vpcState) throws CacheException {
        logger.log(Level.INFO, "Add vpc, Vpc Id:" + vpcState.getId());
        cache.put(vpcState.getId(), vpcState);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete vpc, Vpc Id:" + id);
        cache.remove(id);
    }
}
