/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.ignite.query.MapPredicate;
import com.futurewei.alcor.common.db.ignite.query.ScanQueryBuilder;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.springframework.util.Assert;

import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IgniteClientDbCache<K, V> implements IgniteICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final int RESULT_THRESHOLD_SIZE = 100000;
    private ClientCache<K, V> cache;
    private final IgniteClientTransaction transaction;

    public IgniteClientDbCache(IgniteClient igniteClient, String name) {
        try {
            this.cache = igniteClient.getOrCreateCache(name);
            logger.log(Level.INFO, "Cache " + name + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + name + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + name + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, CacheConfiguration cacheConfig) {
        try {
            ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
            clientCacheConfig.setName(cacheConfig.getName());
            clientCacheConfig.setAtomicityMode(cacheConfig.getAtomicityMode());
            logger.log(Level.INFO, "Getting or creating cache " + clientCacheConfig.getName() + " AtomicityMode is " + clientCacheConfig.getAtomicityMode());
            this.cache = igniteClient.getOrCreateCache(clientCacheConfig);
            logger.log(Level.INFO, "Retrieved cache " +  this.cache.getConfiguration().getName() + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.SEVERE, "Create cache for client " + cacheConfig.getName() + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + cacheConfig.getName() + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, String name, ExpiryPolicy ep) {
        try {
            this.cache = igniteClient.getOrCreateCache(name).withExpirePolicy(ep);
            logger.log(Level.INFO, "Cache " + name + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.SEVERE, "Create cache for client " + name + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + name + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
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
    public Boolean putIfAbsent(K var1, V var2) throws CacheException {
        return cache.putIfAbsent(var1, var2);
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
        return cache.size(CachePeekMode.ALL);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
