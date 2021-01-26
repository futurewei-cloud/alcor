/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.common.db.repo;

import com.futurewei.alcor.common.db.CacheException;

import java.util.List;
import java.util.Map;

public interface ICacheRepository<T> {

    T findItem(String id) throws CacheException;

    Map<String, T> findAllItems() throws CacheException;

    Map<String, T> findAllItems(Map<String, Object[]> queryParams) throws CacheException;

    void addItem(T newItem) throws CacheException;

    void addItems(List<T> items) throws CacheException;

    void deleteItem(String id) throws CacheException;
}
