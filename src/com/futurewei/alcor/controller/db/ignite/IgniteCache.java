package com.futurewei.alcor.controller.db.ignite;

import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.db.Transaction;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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
        }
        catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for vpc failed:" + e.getMessage());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        transaction = new IgniteTransaction(igniteClient);

        Assert.notNull(igniteClient, "Create cache for vpc failed");
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            return cache.get(key);
        }catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache get operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            cache.put(key, value);
        }catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache put operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        try {
            return cache.containsKey(key);
        }catch (ClientException e) {
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
        }catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache putAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return cache.remove(key);
        }catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache remove operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
