/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void addItems(List<QuotaUsageEntity> items) throws CacheException {
        Map<String, QuotaUsageEntity> quotaUsageEntityMap = items.stream().collect(Collectors.toMap(QuotaUsageEntity::getId, Function.identity()));
        cache.putAll(quotaUsageEntityMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }

    @Override
    public void addAllItem(Map<String, QuotaUsageEntity> newItems) throws CacheException {
        cache.putAll(newItems);
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
