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

import java.util.Map;
import java.util.Set;

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

    void put(K var1, V var2) throws CacheException;

    /**
     * Atmoic put entry
     *
     * @param var1 key
     * @param var2 value
     * @return true if no exist false if existed
     * @throws CacheException
     */
    Boolean putIfAbsent(K var1, V var2) throws CacheException;

    boolean containsKey(K var1) throws CacheException;

    /**
     * Get Cache multi keys
     *
     * @param keys
     * @return
     * @throws CacheException
     */
    Map<K, V> getAll(Set<K> keys) throws CacheException;

    Map<K, V> getAll() throws CacheException;

    /**
     * Get Cache values from cache db by multi params
     *
     * @param filterParams a map of params name and value
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException;

    void putAll(Map<? extends K, ? extends V> var1) throws CacheException;

    boolean remove(K var1) throws CacheException;

    /**
     * db cache size
     * @return
     */
    long size();

    Transaction getTransaction();
}
