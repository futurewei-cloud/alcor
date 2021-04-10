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
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void addItems(List<ApplyInfo> items) throws CacheException {
        Map<String, ApplyInfo> applyInfoMap = items.stream().collect(Collectors.toMap(ApplyInfo::getApplyId, Function.identity()));
        cache.putAll(applyInfoMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }

    @Override
    public void addAllItem(Map<String, ApplyInfo> newItems) throws CacheException {
        cache.putAll(newItems);
    }
}
