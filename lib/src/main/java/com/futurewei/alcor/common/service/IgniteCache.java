/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.common.service;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.ICache;
import com.futurewei.alcor.common.repo.Transaction;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.springframework.util.Assert;

import javax.cache.Cache;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IgniteCache<K, V> implements ICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();
    private ClientCache<K, V> cache;
    private IgniteClient igniteClient;
    private IgniteTransaction transaction;

    public IgniteCache(IgniteClient igniteClient, String name) {
        this.igniteClient = igniteClient;

        try {
            cache = igniteClient.getOrCreateCache(name);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for vpc failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        transaction = new IgniteTransaction(igniteClient);

        Assert.notNull(igniteClient, "Create cache for vpc failed");
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            return cache.get(key);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache get operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            cache.put(key, value);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache put operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        try {
            return cache.containsKey(key);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache containsKey operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Map<K, V> getAll() throws CacheException {
        Query<Cache.Entry<K, V>> qry = new ScanQuery<K, V>();

        try {
            QueryCursor<Cache.Entry<K, V>> cur = cache.query(qry);
            return cur.getAll().stream().collect(Collectors
                    .toMap(Cache.Entry::getKey, Cache.Entry::getValue));
        } catch (Exception e) {
            logger.log(Level.WARNING, "IgniteCache getAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) throws CacheException {
        try {
            cache.putAll(items);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache putAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return cache.remove(key);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache remove operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
