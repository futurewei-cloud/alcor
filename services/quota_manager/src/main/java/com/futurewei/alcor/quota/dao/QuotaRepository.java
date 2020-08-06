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
import com.futurewei.alcor.common.db.repo.ICacheRepositoryEx;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.Set;

@Repository
public class QuotaRepository implements ICacheRepositoryEx {

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Boolean putIfAbsent(Object newItem) throws CacheException {
        return null;
    }

    @Override
    public Map findAllItems(Set keys) throws CacheException {
        return null;
    }

    @Override
    public Boolean contains(String key) throws CacheException {
        return null;
    }

    @Override
    public void addAllItem(Map newItems) throws CacheException {

    }

    @Override
    public Object findItem(String id) throws CacheException {
        return null;
    }

    @Override
    public Map findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map findAllItems(Map queryParams) throws CacheException {
        return null;
    }

    @Override
    public void addItem(Object newItem) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {

    }
}
