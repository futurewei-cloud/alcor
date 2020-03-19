package com.futurewei.alcor.controller.db;

import com.futurewei.alcor.controller.exception.CacheException;

public interface Transaction {
    void start() throws CacheException;

    void commit() throws CacheException;

    void rollback() throws CacheException;

    void close();
}
