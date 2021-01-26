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
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class QuotaRepository implements ICacheRepositoryEx<QuotaEntity> {

    private final ICache<String, QuotaEntity> cache;

    @Autowired
    public QuotaRepository(ICacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(QuotaEntity.class);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Boolean putIfAbsent(QuotaEntity newItem) throws CacheException {
        return cache.putIfAbsent(newItem.getId(), newItem);
    }

    @Override
    public Map<String, QuotaEntity> findAllItems(Set<String> ids) throws CacheException {
        return cache.getAll(ids);
    }

    @Override
    public Boolean contains(String id) throws CacheException {
        return cache.containsKey(id);
    }

    @Override
    public QuotaEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, QuotaEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, QuotaEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(QuotaEntity newItem) throws CacheException {
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void addItems(List<QuotaEntity> items) throws CacheException {
        Map<String, QuotaEntity> quotaEntityMap = items.stream().collect(Collectors.toMap(QuotaEntity::getId, Function.identity()));
        cache.putAll(quotaEntityMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }

    @Override
    public void addAllItem(Map<String, QuotaEntity> newItems) throws CacheException {
        cache.putAll(newItems);
    }

    public void addAll(Map<String, QuotaEntity> newItems) throws CacheException {
        cache.putAll(newItems);
    }

    public Map<String, QuotaEntity> findProjectQuotas(String projectId) throws CacheException {
        Map<String, Object[]> map = new HashMap<>();
        map.put("projectId", new String[] {projectId});
        return findAllItems(map);
    }

    public Map<String, QuotaEntity> findProjectQuotas(String projectId, String[] resources) throws CacheException {
        Map<String, Object[]> map = new HashMap<>();
        map.put("projectId", new String[] {projectId});
        map.put("resource", resources);
        return findAllItems(map);
    }
}
