package com.futurewei.alioth.controller.cache.repo;

import com.futurewei.alioth.controller.model.VpcState;

import java.util.Map;

public interface ICacheRepository<T> {

    T findItem(String id);

    Map<String, T> findAllItems();

    void addItem(T newItem);

    void deleteItem(String id);
}
