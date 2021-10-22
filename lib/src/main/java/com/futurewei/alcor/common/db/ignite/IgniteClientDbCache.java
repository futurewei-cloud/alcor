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
import com.google.common.reflect.TypeToken;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.processors.query.QueryUtils;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.checkerframework.checker.units.qual.C;
import org.springframework.util.Assert;

import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import javax.swing.*;
import javax.xml.validation.TypeInfoProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IgniteClientDbCache<K, V> implements IgniteICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final int RESULT_THRESHOLD_SIZE = 100000;
    private ClientCache<K, V> cache;
    private final IgniteClientTransaction transaction;

    public IgniteClientDbCache(IgniteClient igniteClient, String name) {
        try {
            Map<String, String> sqlFields = getSqlFields();
            if (!sqlFields.isEmpty()) {
                this.cache = getOrCreateIndexedCache(igniteClient, sqlFields, name);
            }
            if (this.cache == null)
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
            Map<String, String> sqlFields = getSqlFields();
            if (!sqlFields.isEmpty()) {
                this.cache = getOrCreateIndexedCache(igniteClient, sqlFields, clientCacheConfig);
            }
            if (this.cache == null)
                this.cache = igniteClient.getOrCreateCache(clientCacheConfig);

            logger.log(Level.INFO, "Retrieved cache " +  this.cache.getConfiguration().getName() + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + cacheConfig.getName() + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + cacheConfig.getName() + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, String name, ExpiryPolicy ep) {
        try {
            this.cache = igniteClient.getOrCreateCache(name).withExpirePolicy(ep);
            logger.log(Level.INFO, "Cache " + name + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + name + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + name + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    /**
     *
     * @param igniteClient
     * @param name
     * If the class has QuerySqlField annotations, add query entry fields and indexes.
     */
    private ClientCache<K, V> getOrCreateIndexedCache(IgniteClient igniteClient, Map<String, String> sqlFields, String name) {
        logger.log(Level.INFO, "Creating cache " + name + " with index");
                ClientCacheConfiguration ccConfig = new ClientCacheConfiguration();
                ccConfig.setName(name);
                return getOrCreateIndexedCacheInternal(igniteClient, sqlFields, ccConfig);
    }

    private ClientCache<K, V> getOrCreateIndexedCache(IgniteClient igniteClient, Map<String, String> sqlFields, ClientCacheConfiguration cacheConfig) {
        logger.log(Level.INFO, "Creating cache " + cacheConfig.getName() + " with index");
        ClientCacheConfiguration ccConfig = new ClientCacheConfiguration();
        ccConfig.setName(cacheConfig.getName());
        ccConfig.setAtomicityMode(cacheConfig.getAtomicityMode());
        return getOrCreateIndexedCacheInternal(igniteClient, sqlFields, ccConfig);
    }


    private Map<String, String> getSqlFields() {
        Map<String, String> sqlFields = new HashMap<>();
        Type t = getClass();
        logger.log(Level.INFO, "Checking for QuerySqlField annotations: " + t.getTypeName());
        Class<V> type = (Class<V>) (new TypeToken<V>(getClass()){}.getType());
        logger.log(Level.INFO, "Checking for QuerySqlField annotations: " + type.getName());
        // go through all fields but pick the very first one, for now,
        // make it work for multiple fileds later.
        for (Field f : type.getDeclaredFields()) {
            QuerySqlField annot = f.getAnnotation(QuerySqlField.class);
            if (annot.notNull()) {
                logger.log(Level.INFO, "annotation: " + annot.toString());
                if (annot.index()) {
                    if (sqlFields.keySet().isEmpty())
                        sqlFields.put("index", annot.name());
                }
                else if (sqlFields.values().isEmpty())
                    sqlFields.put("value", annot.name());
            }
        }
        logger.log(Level.INFO, "Found " + sqlFields.size() + " sqlFields");
        return sqlFields;
    }

    private ClientCache<K, V> getOrCreateIndexedCacheInternal(IgniteClient igniteClient, Map<String, String> sqlFields, ClientCacheConfiguration cachConfig) {

        QueryEntity qryEnt = new QueryEntity();
        String idxFld = sqlFields.get("index");
        String valFld = sqlFields.get("value");

        Class<V> type = (Class<V>) (new TypeToken<V>(getClass()){}.getType());
        String className = type.getName();
        logger.log(Level.INFO, "IndexedCache: " + cachConfig.getName() + " IDX " + idxFld + " VAL " + valFld + " TYP " + className);
        try {
            qryEnt.setKeyType(String.valueOf(type.getField(idxFld)));
            qryEnt.setValueType(type.getClass().getName());
            qryEnt.addQueryField(idxFld, String.valueOf(type.getField(idxFld)), null);
            qryEnt.addQueryField(valFld, String.valueOf(type.getField(valFld)), null);
            QueryIndex qryIndex = new QueryIndex(idxFld);
            qryEnt.setIndexes(Collections.singleton(qryIndex));
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create index on cache: " + cachConfig.getName() + ": " + e.getMessage());
            return null;
        }

        cachConfig.setQueryEntities(qryEnt);

        // also make not of this cache, somewhere, somehow?
        // have a static cache?
        ClientCache<K, V> cache = igniteClient.getOrCreateCache(cachConfig);

        String result = cache == null ? "FAILED" : "WORKED";

        logger.log(Level.INFO, "Creating index on " + cachConfig.getName() + " " + result);

        return cache;
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
        Map<String, String> sqlFields = checkForSqlFieldsQuery(filterParams);
        if (sqlFields != null)
            return getSqlFields(sqlFields, filterParams);

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
        Map<String, String> sqlFields = checkForSqlFieldsQuery(filterParams);
        if (sqlFields != null)
            return getSqlFieldsAll(sqlFields, filterParams);
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

    private Map<String, String> checkForSqlFieldsQuery(Map<String, Object[]> filterParams) {
        Map<String, String> sqlFields = new HashMap<>();
        Class<V> type = (Class<V>) (new TypeToken<V>(getClass()) {}.getType());
        for (Field f : type.getDeclaredFields()) {
            QuerySqlField annot = f.getAnnotation(QuerySqlField.class);
            if (annot.notNull()) {
                if (annot.index())
                    sqlFields.put("key", annot.name());
                else
                    sqlFields.put("value", annot.name());
            }
        }

        /*
         * There must be exactly two sqlfileds, one for the index lookup
         * and the other for the _VAL (V) field in the Class declaration.
         * There can only one be exactly one field in queryParams for now.
         * The entry in the filterparams should be an indexed field.
         * If these conditions are true, run SQLFieldsQuery otherwise, ScanQuery.
         */
        if (sqlFields.size() == 2 && filterParams.size() == 1 && sqlFields.containsKey(filterParams.keySet())) {
            return sqlFields;
        }

        return null;
    }

    private <E1, E2> V getSqlFields(Map<String, String> sqlFields, Map<String, Object[]> filterParams) {
        try {
            Map<K, V> result = runSQLFieldsQuery(sqlFields, filterParams);
            if (result == null || result.isEmpty())
                return null;
            return (V) result.get(0);
        }
        catch (Exception e) {
            return null;
        }
    }

    public <E1, E2> Map<K, V> getSqlFieldsAll(Map<String, String> sqlFields, Map<String, Object[]> filterParams) {
        try {
            Map<K, V> values = runSQLFieldsQuery(sqlFields, filterParams);
            return values;
        }
        catch (Exception e) {
            return null;
        }
    }

    /*
    * SELECT value_sqlfield
    * FROM "ClassNameOfTheCache".classnameofthecache
    * WHERE key_sqlfield = filterParam.value
     */
    private <E1, E2> Map<K, V> runSQLFieldsQuery(Map<String, String> sqlFields, Map<String, Object[]> filterParams) throws CacheException {

        String sql = buildSqlFieldsQuery(sqlFields, filterParams);
        SqlFieldsQuery query = new SqlFieldsQuery(sql);
        Map<K, V> results = new HashMap<>();

        try (QueryCursor<List<?>> cursor = cache.query(query)) {
            for (List<?> row : cursor) {
                results.put((K)row.get(0), (V)row.get(1));
            }
            return results;
        }
        catch (ClientException e) {
            logger.log(Level.WARNING, "SqlFieldsQuery error: " + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    private String buildSqlFieldsQuery(Map<String, String> sqlFields, Map<String, Object[]> filterParams){
        StringBuilder sb = new StringBuilder("select ");
        sb.append(sqlFields.get("value")).append(" from \"");
        sb.append(cache.getConfiguration().getName()).append("\"");
        sb.append(" where ");
        sb.append(filterParams.keySet()).append(" = ");
        sb.append(filterParams.values());

        return sb.toString();
    }
}