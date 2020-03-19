package com.futurewei.alcor.controller.db;

import com.futurewei.alcor.controller.exception.CacheException;

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
