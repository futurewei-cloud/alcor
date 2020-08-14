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
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class ApplyRepository implements ICacheRepositoryEx<ApplyInfo> {

    private final ICache<String, ApplyInfo> cache;

    @Autowired
    public ApplyRepository(ICacheFactory cacheFactory) {
        cache = cacheFactory.getExpireCache(ApplyInfo.class, 1, TimeUnit.DAYS);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Boolean putIfAbsent(ApplyInfo newItem) throws CacheException {
        return cache.putIfAbsent(newItem.getApplyId(), newItem);
    }

    @Override
    public Map<String, ApplyInfo> findAllItems(Set<String> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    public Boolean contains(String key) throws CacheException {
        return cache.containsKey(key);
    }

    @Override
    public ApplyInfo findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, ApplyInfo> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, ApplyInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(ApplyInfo newItem) throws CacheException {
        cache.put(newItem.getApplyId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
