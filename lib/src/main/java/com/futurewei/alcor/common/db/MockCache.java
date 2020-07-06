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

import java.util.HashMap;
import java.util.Map;

public class MockCache<K, V> implements ICache<K, V> {
    private Map<K, V> cache = new HashMap<>();
    private Transaction transaction;

    public MockCache(Transaction transaction) {
        this.transaction = transaction;
    }

    public MockCache() {
    }

    @Override
    public V get(K key) throws CacheException {
        return cache.get(key);
    }

    @Override
    public void put(K key, V value) throws CacheException {
        cache.put(key, value);
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        return cache.containsKey(key);
    }

    @Override
    public Map<K, V> getAll() throws CacheException {
        return cache;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) throws CacheException {
        cache.putAll(items);
    }

    @Override
    public boolean remove(K key) throws CacheException {
        cache.remove(key);
        return true;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
