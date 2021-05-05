/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.elasticipmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.entity.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public abstract class RepoResource<T extends Resource> implements ICacheRepository<T> {
    private static final Logger LOG = LoggerFactory.getLogger(RepoResource.class);
    private ICache<String, T> cache;

    public abstract Class<T> getResourceClass();

    public RepoResource(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(getResourceClass());
    }

    @Override
    public T findItem(String id) {
        try {
            return cache.get(id);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error(this.getClass().getName() + " findItem() exception:", e);
        }

        return null;
    }

    @Override
    public Map<String, T> findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error(this.getClass().getName() + " findAllItems() exception:", e);
        }

        return new HashMap<String, T>();
    }

    @Override
    public Map<String, T> findAllItems(Map<String, Object[]> queryParams) {
        try {
            return cache.getAll(queryParams);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error(this.getClass().getName() + " findAllItems(queryParams) exception:", e);
        }

        return new HashMap<String, T>();
    }


    @Override
    public void addItem(T resource) {
        try {
            cache.put(resource.getId(), resource);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error(this.getClass().getName() + " addItem() exception:", e);
        }
    }

    @Override
    public void deleteItem(String resourceId) {
        try {
            cache.remove(resourceId);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error(this.getClass().getName() + " deleteItem() exception:", e);
        }
    }
}
