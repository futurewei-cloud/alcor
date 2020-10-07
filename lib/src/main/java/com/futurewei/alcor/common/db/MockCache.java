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

import com.futurewei.alcor.common.db.ignite.IgniteTransaction;
import com.futurewei.alcor.common.entity.CustomerResource;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

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
    public Boolean putIfAbsent(K var1, V var2) throws CacheException {
        return cache.putIfAbsent(var1, var2).equals(var2);
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) throws CacheException {
        Map<K, V> filterMap = new HashMap<>();
        for(K k: keys){
            filterMap.put(k, cache.getOrDefault(k, null));
        }
        return filterMap;
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public V get(Map<String, Object[]> filterParams) throws CacheException {
        Collection<V> collection = filter(filterParams).values();
        Object[] vs = collection.toArray();
        return vs.length == 1 ? (V)vs[0]: null;
    }

    @Override
    public <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException {
        return filter(filterParams);
    }

    private Map<K, V> filter(Map<String, Object[]> filterParams){
        Map<K, V> result = new HashMap<>();

        for(Map.Entry<K, V> entry: cache.entrySet()){
            for(Map.Entry<String, Object[]> filterEntry: filterParams.entrySet()){
                Object value = getObjectFieldValue(entry.getValue(), filterEntry.getKey());
                if(value == null){
                    continue;
                }

                boolean fieldMatch = false;
                for(Object expectValue: filterEntry.getValue()){
                    fieldMatch |= expectValue.equals(value);
                }

                if(fieldMatch){
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    private Object getObjectFieldValue(Object obj, String fieldName){
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        if(field == null){
            return null;
        }
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
