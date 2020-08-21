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

package com.futurewei.alcor.common.db.repo;


import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.repo.ICacheRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICacheRepositoryEx<T> extends ICacheRepository<T> {

    long size();

    Boolean putIfAbsent(T newItem) throws CacheException;

    Map<String, T> findAllItems(Set<String> keys) throws CacheException;

    Boolean contains(String key) throws CacheException;

    void addAllItem(Map<String, T> newItems) throws CacheException;
}
