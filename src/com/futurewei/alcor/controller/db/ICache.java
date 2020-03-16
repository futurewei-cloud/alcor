package com.futurewei.alcor.controller.db;

import java.util.Map;

public interface ICache<K, V> {
    V get(K var1);

    void put(K var1, V var2);

    boolean containsKey(K var1);

    Map<K, V> getAll();

    void putAll(Map<? extends K, ? extends V> var1);

    boolean remove(K var1);

}
