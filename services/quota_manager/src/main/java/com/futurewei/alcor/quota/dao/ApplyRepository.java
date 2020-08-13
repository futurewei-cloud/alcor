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
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

@Repository
public class ApplyRepository implements ICacheRepositoryEx<ApplyInfo> {

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Boolean putIfAbsent(ApplyInfo newItem) throws CacheException {
        return null;
    }

    @Override
    public Map<String, ApplyInfo> findAllItems(Set<String> keys) throws CacheException {
        return null;
    }

    @Override
    public Boolean contains(String key) throws CacheException {
        return null;
    }

    @Override
    public ApplyInfo findItem(String id) throws CacheException {
        return null;
    }

    @Override
    public Map<String, ApplyInfo> findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map<String, ApplyInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return null;
    }

    @Override
    public void addItem(ApplyInfo newItem) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {

    }
}
