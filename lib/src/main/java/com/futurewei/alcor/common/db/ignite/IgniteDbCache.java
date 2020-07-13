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

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.ignite.query.ScanQueryBuilder;
import com.futurewei.alcor.common.db.ignite.query.MapPredicate;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.transactions.TransactionException;
import org.springframework.util.Assert;

import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class IgniteDbCache<K, V> implements IgniteICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final int RESULT_THRESHOLD_SIZE = 100000;
    private IgniteCache<K, V> cache;
    private final IgniteTransaction transaction;

    public IgniteDbCache(Ignite ignite, String name) {

        try {
            this.cache = ignite.getOrCreateCache(name);
        } catch (javax.cache.CacheException e) {
            logger.log(Level.WARNING, "Create cache for client " + name + " failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(cache, "Create cache for client " + name + "failed");
        this.transaction = new IgniteTransaction(ignite);
    }

    public IgniteDbCache(Ignite client, String name, ExpiryPolicy ep) {

        try {
            this.cache = client.getOrCreateCache(name);
            this.cache = this.cache.withExpiryPolicy(ep);
        } catch (javax.cache.CacheException e) {
            logger.log(Level.WARNING, "Create cache for client " + name + " failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + name + "failed");
        this.transaction = new IgniteTransaction(client);
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            return cache.get(key);
        } catch (TransactionException e) {
            logger.log(Level.WARNING, "IgniteCache get operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            cache.put(key, value);
        } catch (TransactionException e) {
            logger.log(Level.WARNING, "IgniteCache put operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Boolean putIfAbsent(K var1, V var2) throws CacheException {
        return cache.putIfAbsent(var1, var2);
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        try {
            return cache.containsKey(key);
        } catch (TransactionException e) {
            logger.log(Level.WARNING, "IgniteCache containsKey operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) throws CacheException {
        return cache.getAll(keys);
    }

    @Override
    public Map<K, V> getAll() throws CacheException {
        Query<Cache.Entry<K, V>> qry = new ScanQuery<>();

        QueryCursor<Cache.Entry<K, V>> cur = cache.query(qry);
        return cur.getAll().stream().collect(Collectors
                .toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) throws CacheException {
        try {
            cache.putAll(items);
        } catch (IgniteException e) {
            logger.log(Level.WARNING, "IgniteCache putAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return cache.remove(key);
        } catch (IgniteException e) {
            logger.log(Level.WARNING, "IgniteCache remove operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public V get(Map<String, Object[]> filterParams) throws CacheException {
        IgniteBiPredicate<String, BinaryObject> predicate = MapPredicate.getInstance(filterParams);
        return get(predicate);
    }

    @Override
    public <E1, E2> V get(IgniteBiPredicate<E1, E2> igniteBiPredicate) throws CacheException {
        QueryCursor<Cache.Entry<E1, E2>> cursor =
                cache.withKeepBinary().query(ScanQueryBuilder.newScanQuery(igniteBiPredicate));
        List<Cache.Entry<E1, E2>> result = cursor.getAll();
        if(result.size() > 1){
            throw new CacheException("more than one rows found!");
        }

        if(result.isEmpty()){
            return null;
        }

        E2 obj = result.get(0).getValue();
        if (obj instanceof BinaryObject){
            BinaryObject binaryObject = (BinaryObject)obj;
            return binaryObject.deserialize();
        }else{
            throw new CacheException("no support for object type:" + obj.getClass().getName());
        }
    }

    @Override
    public <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException {
        IgniteBiPredicate<String, BinaryObject> predicate = MapPredicate.getInstance(filterParams);
        return getAll(predicate);
    }

    @Override
    public <E1, E2> Map<K, V> getAll(IgniteBiPredicate<E1, E2> igniteBiPredicate) throws CacheException {
        QueryCursor<Cache.Entry<E1, E2>> cursor =
                cache.withKeepBinary().query(ScanQueryBuilder.newScanQuery(igniteBiPredicate));
        List<Cache.Entry<E1, E2>> result = cursor.getAll();
        if(result.size() >= RESULT_THRESHOLD_SIZE){
            throw new CacheException("too many rows found!");
        }
        Map<K, V> values = new HashMap<>(result.size());
        for(Cache.Entry<E1, E2> entry: result){
            E2 obj = entry.getValue();
            if (obj instanceof BinaryObject){
                BinaryObject binaryObject = (BinaryObject)obj;
                values.put((K)entry.getKey(), binaryObject.deserialize());
            }else{
                throw new CacheException("no support for object type:" + obj.getClass().getName());
            }
        }
        return values;
    }

    @Override
    public long size() {
        cache.destroy();
        return cache.size(CachePeekMode.ALL);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
