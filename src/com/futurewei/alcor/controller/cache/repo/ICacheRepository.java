package com.futurewei.alcor.controller.cache.repo;

import java.util.Map;

public interface ICacheRepository<T> {

    T findItem(String id);

    Map<String, T> findAllItems();

    void addItem(T newItem);

    void deleteItem(String id);
}
