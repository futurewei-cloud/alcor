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

package com.futurewei.alcor.common.db;


import com.futurewei.alcor.common.db.query.CachePredicate;

import java.util.Map;

public interface ICache<K, V> {
    V get(K var1) throws CacheException;

    /**
     * Get Cache value from cache db by multi params
     *
     * @param filterParams a map of params name and value
     * @return cache value
     * @throws CacheException if any exception
     */
    V get(Map<String, Object[]> filterParams) throws CacheException;

    /**
     * Get Cache value from cache db by a {@link CachePredicate}
     *
     * @param cachePredicate a implement of {@link CachePredicate}
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> V get(CachePredicate<E1, E2> cachePredicate) throws CacheException;

    void put(K var1, V var2) throws CacheException;

    boolean containsKey(K var1) throws CacheException;

    Map<K, V> getAll() throws CacheException;

    /**
     * Get Cache values from cache db by multi params
     *
     * @param filterParams a map of params name and value
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException;

    /**
     * Get Cache values from cache db by a {@link CachePredicate}
     *
     * @param cachePredicate a implement of {@link CachePredicate}
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> Map<K, V> getAll(CachePredicate<E1, E2> cachePredicate) throws CacheException;

    void putAll(Map<? extends K, ? extends V> var1) throws CacheException;

    boolean remove(K var1) throws CacheException;

    Transaction getTransaction();
}
