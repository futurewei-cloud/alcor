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
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.google.common.reflect.TypeToken;
import io.netty.util.concurrent.EventExecutorChooserFactory;
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
import org.apache.ignite.lang.IgniteBiPredicate;
import org.springframework.util.Assert;

import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IgniteClientDbCache<K, V> implements IgniteICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private static final int RESULT_THRESHOLD_SIZE = 100000;
    private ClientCache<K, V> cache;
    private final IgniteClientTransaction transaction;
    private class SqlField {
        public String name;
        public String type;
    }
    private Map<String, SqlField> sqlFields = null;
    private boolean checkedForSqlFields = false;

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, String name) {
        String className = v.getName();
        try {
            if (checkedForSqlFields == false) {
                checkedForSqlFields = true;
                extractSqlFields(className);
                if (sqlFields != null && sqlFields.size() != 0) {
                    ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
                    clientCacheConfig.setName(className);
                    this.cache = getOrCreateIndexedCache(igniteClient, className, clientCacheConfig, null);
                }
            }
            if (this.cache == null)
                this.cache = igniteClient.getOrCreateCache(className);

            logger.log(Level.INFO, "Cache " + className + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + className + " failed:" + e.getMessage());
            logger.log(Level.WARNING, "Create cache for client " + className + " failed:" + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + className + "failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, CacheConfiguration cacheConfig) {
        try {
            String className = v.getName();
            ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
            clientCacheConfig.setName(cacheConfig.getName());
            clientCacheConfig.setAtomicityMode(cacheConfig.getAtomicityMode());
            logger.log(Level.INFO, "Getting or creating cache " + clientCacheConfig.getName() + " AtomicityMode is " + clientCacheConfig.getAtomicityMode());
            extractSqlFields(className);
            if (sqlFields != null) {
                this.cache = getOrCreateIndexedCache(igniteClient, className, clientCacheConfig, null);
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

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, String name, ExpiryPolicy ep) {
        try {
            if (checkedForSqlFields == false) {
                checkedForSqlFields = true;
                extractSqlFields(v.getName());
                if (sqlFields != null) {
                    ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
                    clientCacheConfig.setName(name);
                    getOrCreateIndexedCache(igniteClient, v.getName(), clientCacheConfig, ep);
                }
            }
            if (this.cache == null) {
                this.cache = igniteClient.getOrCreateCache(name).withExpirePolicy(ep);
                logger.log(Level.INFO, "Cache " + name + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
            }
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + name + " failed:" + e.getMessage());
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
        if (checkForSqlFieldsQuery(filterParams) == true) {
            return getBySqlFields(filterParams);
        }
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
        if (checkForSqlFieldsQuery(filterParams) == true) {
            return getBySqlFieldsAll(filterParams);
        }
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

    private boolean checkForSqlFieldsQuery(Map<String, Object[]> filterParams) {
        if (checkedForSqlFields == true && sqlFields == null)
            return false;
        /*
         * There must be exactly two sqlfileds, one for the index lookup
         * and the other for the _VAL (V) field in the Class declaration.
         * There can only one be exactly one field in queryParams for now.
         * The entry in the filterparams should be an indexed field.
         * If these conditions are true, run SQLFieldsQuery otherwise, ScanQuery.
         */
        if (sqlFields.size() == 2 && filterParams.size() == 1 && sqlFields.containsKey(filterParams.keySet())) {
            return true;
        }

        return false;
    }

    private <E1, E2> V getBySqlFields(Map<String, Object[]> filterParams) {
        try {
            Map<K, V> result = runSQLFieldsQuery(filterParams);
            if (result == null || result.isEmpty())
                return null;
            return (V) result.get(0);
        }
        catch (Exception e) {
            return null;
        }
    }

    public <E1, E2> Map<K, V> getBySqlFieldsAll(Map<String, Object[]> filterParams) {
        try {
            Map<K, V> values = runSQLFieldsQuery(filterParams);
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
    private <E1, E2> Map<K, V> runSQLFieldsQuery(Map<String, Object[]> filterParams) throws CacheException {

        String sql = buildSqlFieldsQuery(filterParams);
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

    private String buildSqlFieldsQuery(Map<String, Object[]> filterParams){
        StringBuilder sb = new StringBuilder("select ");
        sb.append(sqlFields.get("value").name).append(" from \"");
        sb.append(cache.getConfiguration().getName()).append("\"");
        sb.append(" where ");
        sb.append(filterParams.keySet()).append(" = ");
        sb.append(filterParams.values());

        return sb.toString();
    }

    /**
     *
     * @param igniteClient
     * @param className
     * If the class has QuerySqlField annotations, add query entry fields and indexes.
     */
    private ClientCache<K, V> getOrCreateIndexedCache(IgniteClient igniteClient, String className, ClientCacheConfiguration cacheConfig, ExpiryPolicy ep) {
        String cacheName = cacheConfig.getName();
        logger.log(Level.INFO, "Creating cache " + cacheName + " with index");

        SqlField idxFld = sqlFields.get("index");
        SqlField valFld = sqlFields.get("value");
        Class<?> v = null;
        try {
            v = Class.forName(className);
        } catch (Exception e) {
            logger.log(Level.INFO, "Failed to get class for " + className);
            return null;
        }
        QueryEntity qryEnt = null;

        try {
            String tblName = CommonUtil.getSimpleFromCanonicalName(valFld.name);
            qryEnt = new QueryEntity(idxFld.type, valFld.name).setTableName(tblName);
            logger.log(Level.INFO, "Setting table " + tblName + " for cache " + className);
        } catch (Exception e) {
            logger.log(Level.INFO, "Failed to get type for index field " + idxFld.name);
            return null;
        }
        try {
            qryEnt.setKeyFieldName(idxFld.name);
            qryEnt.setValueType(idxFld.type);
            qryEnt.addQueryField(idxFld.name, idxFld.type, null);
            qryEnt.addQueryField(valFld.name, valFld.type, null);
            QueryIndex qryIndex = new QueryIndex(idxFld.name);
            qryEnt.setIndexes(Collections.singleton(qryIndex));
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create index on cache: " + cacheName + ": " + e.getMessage());
            return null;
        }

        cacheConfig.setQueryEntities(qryEnt);
        String schName = CommonUtil.getSchemaNameForCacheClass(v.getName());
        logger.log(Level.INFO, "Setting schema name " + schName + " for cahce " + cacheName);
        cacheConfig.setSqlSchema(schName);

        // also make not of this cache, somewhere, somehow?
        // have a static cache?
        ClientCache<K, V> cache = null;

        if (ep != null) {
            cache = igniteClient.getOrCreateCache(cacheConfig).withExpirePolicy(ep);
        }
        else {
            cache = igniteClient.getOrCreateCache(cacheConfig);
        }

        String result = cache == null ? "FAILED" : "WORKED";

        logger.log(Level.INFO, "Creating index on " + cacheName + " " + result);

        return cache;
    }


    private void extractSqlFields(String className) {
        Map<String, SqlField> localFields = new HashMap<>();
        logger.log(Level.INFO, "Checking for QuerySqlField annotations: " + className);
        Field[] fields = null;
        try {
            Class<?> v = Class.forName(className);

            fields = ControllerUtil.getAllDeclaredFields(v);

            // go through all fields but pick the very first one, for now,
            // make it work for multiple fields later.
            for (Field f : fields) {
                QuerySqlField annot = f.getAnnotation(QuerySqlField.class);
                if (annot != null) {
                    logger.log(Level.INFO, "Found for " + f.getName() + " annotation: " + annot.toString());
                    if (annot.index()) {
                        if (localFields.keySet().isEmpty()) {
                            SqlField sqlField = new SqlField();
                            sqlField.name = f.getName();
                            sqlField.type = f.getType().getTypeName();
                            logger.log(Level.INFO, "Adding index for " + sqlField.name + " with type " + sqlField.type);
                            localFields.put("index", sqlField);
                        }
                    } else {
                        SqlField sqlField = new SqlField();
                        sqlField.name = f.getName();
                        sqlField.type = f.getType().getTypeName();
                        logger.log(Level.INFO, "Adding value for " + sqlField.name + " with type " + sqlField.type);
                        sqlFields.put("value", sqlField);
                    }
                }
            }

            if (localFields.isEmpty() == false && localFields.containsValue(className) == false) {
                SqlField sqlField = new SqlField();
                sqlField.name = className;
                sqlField.type = v.getTypeName();
                logger.log(Level.INFO, "Adding value for " + sqlField.name + " with type " + sqlField.type);
                localFields.put("value", sqlField);
            }
            sqlFields = localFields;
            logger.log(Level.INFO, "Found " + sqlFields.size() + " sqlFields");
        } catch (Exception e) {
            //
        }
    }
}