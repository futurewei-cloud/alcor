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

package com.futurewei.alcor.common.repo;

import com.futurewei.alcor.common.exception.CacheException;

import java.util.Map;

public interface ICache<K, V> {
    V get(K var1) throws CacheException;

    void put(K var1, V var2) throws CacheException;

    boolean containsKey(K var1) throws CacheException;

    Map<K, V> getAll() throws CacheException;

    void putAll(Map<? extends K, ? extends V> var1) throws CacheException;

    boolean remove(K var1) throws CacheException;

    Transaction getTransaction();
}
