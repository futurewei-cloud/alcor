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

package com.futurewei.alcor.quota.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepositoryEx;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

@Repository
public class QuotaRepository implements ICacheRepositoryEx<QuotaEntity> {

    private final ICache<String, QuotaEntity> cache;

    @Autowired
    public QuotaRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(QuotaEntity.class);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Boolean putIfAbsent(QuotaEntity newItem) throws CacheException {
        return cache.putIfAbsent(newItem.getProjectId(), newItem);
    }

    @Override
    public Map<String, QuotaEntity> findAllItems(Set<String> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    public Boolean contains(String key) throws CacheException {
        return cache.containsKey(key);
    }

    @Override
    public QuotaEntity findItem(String projectId) throws CacheException {
        return cache.get(projectId);
    }

    @Override
    public Map<String, QuotaEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(QuotaEntity newItem) throws CacheException {
        cache.put(newItem.getProjectId(), newItem);
    }

    @Override
    public Map<String, QuotaEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void deleteItem(String projectId) throws CacheException {
        cache.remove(projectId);
    }
}
