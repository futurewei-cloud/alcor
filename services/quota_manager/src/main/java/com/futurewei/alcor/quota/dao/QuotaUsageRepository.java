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
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.repo.ICacheRepositoryEx;
import com.futurewei.alcor.web.entity.quota.QuotaUsageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class QuotaUsageRepository implements ICacheRepositoryEx<QuotaUsageEntity> {

    private final ICache<String, QuotaUsageEntity> cache;

    @Autowired
    public QuotaUsageRepository(ICacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(QuotaUsageEntity.class);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Boolean putIfAbsent(QuotaUsageEntity newItem) throws CacheException {
        return cache.putIfAbsent(newItem.getId(), newItem);
    }

    @Override
    public Map<String, QuotaUsageEntity> findAllItems(Set<String> ids) throws CacheException {
        return cache.getAll(ids);
    }

    @Override
    public Boolean contains(String id) throws CacheException {
        return cache.containsKey(id);
    }

    @Override
    public QuotaUsageEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, QuotaUsageEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, QuotaUsageEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(QuotaUsageEntity newItem) throws CacheException {
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }

    public void addAll(Map<String, QuotaUsageEntity> quotaUsages) throws CacheException {
        cache.putAll(quotaUsages);
    }

    public Map<String, QuotaUsageEntity> findProjectQuotas(String projectId) throws CacheException {
        Map<String, Object[]> map = new HashMap<>();
        map.put("projectId", new String[] {projectId});
        return findAllItems(map);
    }

    public Map<String, QuotaUsageEntity> findProjectQuotas(String projectId, String[] resources) throws CacheException {
        Map<String, Object[]> map = new HashMap<>();
        map.put("projectId", new String[] {projectId});
        map.put("resource", resources);
        return findAllItems(map);
    }
}
