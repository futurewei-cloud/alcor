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
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.common.utils.ControllerUtil;
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
import java.sql.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class IgniteClientDbCache<K, V> implements IgniteICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final String NON_SCALAR_ROWSET = "too many rows found!";
    private static final int RESULT_THRESHOLD_SIZE = 100000;
    private static final String SQL_SCHEMA_NAME = "alcor";
    private static final int    SQL_INDEX_MAX_INLINE_SIZE = 36; // UUID length
    private ClientCache<K, V> cache;
    private final IgniteClientTransaction transaction;
    private static class SqlField {
        public String type;
        public boolean isIndexed;
    }
    private LinkedHashMap<String, SqlField> sqlFields = null; // needed for index creation and querying
    private boolean checkedForSqlFields = false;

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, String name) {
        String className = v.getName();
        try {
            if (!checkedForSqlFields) {
                checkedForSqlFields = true;
                extractSqlFields(className);
                if (sqlFields != null && sqlFields.size() != 0) {
                    ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
                    clientCacheConfig.setName(CommonUtil.getSqlNameFromCacheName(name));
                    this.cache = getOrCreateIndexedCache(igniteClient, className, clientCacheConfig, null);
                    if (this.cache == null) {
                        logger.log(Level.WARNING, "Create cache for client " + className + " with index failed, falling back");
                    }
                }
            }
            if (this.cache == null)
                this.cache = igniteClient.getOrCreateCache(className);

        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + className + " failed: " + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + className + " failed");
        logger.log(Level.INFO, "Cache " + className + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, CacheConfiguration cacheConfig) {
        try {
            String className = v.getName();
            ClientCacheConfiguration clientCacheConfig = new ClientCacheConfiguration();
            clientCacheConfig.setName(CommonUtil.getSqlNameFromCacheName(cacheConfig.getName()));
            clientCacheConfig.setAtomicityMode(cacheConfig.getAtomicityMode());
            logger.log(Level.INFO, "Getting or creating cache " + clientCacheConfig.getName() + " AtomicityMode is " + clientCacheConfig.getAtomicityMode());
            if (!checkedForSqlFields) {
                checkedForSqlFields = true;
                extractSqlFields(className);
                if (sqlFields != null && sqlFields.size() != 0) {
                    this.cache = getOrCreateIndexedCache(igniteClient, className, clientCacheConfig, null);
                    if (this.cache == null) {
                        logger.log(Level.WARNING, "Create cache for client " + className + " with index failed, falling back");
                    }
                }
            }
            if (this.cache == null)
                this.cache = igniteClient.getOrCreateCache(clientCacheConfig);

        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for client " + cacheConfig.getName() + " failed: " + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + cacheConfig.getName() + " failed");
        logger.log(Level.INFO, "Retrieved cache " +  this.cache.getConfiguration().getName() + " AtomicityMode is " + this.cache.getConfiguration().getAtomicityMode());
        this.transaction = new IgniteClientTransaction(igniteClient);
    }

    public IgniteClientDbCache(IgniteClient igniteClient, Class<?> v, String name, ExpiryPolicy ep) {
        try {
            if (!checkedForSqlFields) {
                checkedForSqlFields = true;
                extractSqlFields(v.getName());
                if (sqlFields != null && sqlFields.size() != 0) {
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
            logger.log(Level.WARNING, "Create cache for client " + name + " failed: " + e.getMessage());
        }

        Assert.notNull(this.cache, "Create cache for client " + name + " failed");
        this.transaction = new IgniteClientTransaction(igniteClient);
    }


    @Override
    public V get(K key) throws CacheException {
        try {
            return cache.get(key);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache get operation error: " + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            cache.put(key, value);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache put operation error: " + e.getMessage());
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
            logger.log(Level.WARNING, "IgniteCache containsKey operation error: " + e.getMessage());
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
            logger.log(Level.WARNING, "IgniteCache putAll operation error: " + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return cache.remove(key);
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteCache remove operation error: " + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public V get(Map<String, Object[]> filterParams) throws CacheException {
        if (checkForSqlFieldsQuery(filterParams)) {
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
            throw new CacheException(NON_SCALAR_ROWSET);
        }

        if(result.isEmpty()){
            return null;
        }

        E2 obj = result.get(0).getValue();
        if (obj instanceof BinaryObject){
            BinaryObject binaryObject = (BinaryObject)obj;
            return binaryObject.deserialize();
        }else{
            throw new CacheException("no support for object type: " + obj.getClass().getName());
        }
    }

    @Override
    public <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException {
        if (checkForSqlFieldsQuery(filterParams)) {
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
            throw new CacheException(NON_SCALAR_ROWSET);
        }
        Map<K, V> values = new HashMap<>(result.size());
        for(Cache.Entry<E1, E2> entry: result){
            E2 obj = entry.getValue();
            if (obj instanceof BinaryObject){
                BinaryObject binaryObject = (BinaryObject)obj;
                values.put((K)entry.getKey(), binaryObject.deserialize());
            }else{
                throw new CacheException("no support for object type: " + obj.getClass().getName());
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
        if (!checkedForSqlFields)
            return false;
        if (sqlFields == null || sqlFields.size() == 0 || filterParams.size() == 0)
            return false;

        /*
        * All filterParams must be found in sqlfields as "index".
         */
        int idxCount = 0;
        for (String f : filterParams.keySet()) {
            if (sqlFields.containsKey(f))
                ++idxCount;
        }

        return idxCount != 0 && idxCount == filterParams.size();
    }

    @DurationStatistics
    private <E1, E2> V getBySqlFields(Map<String, Object[]> filterParams) throws CacheException {
        Map<K, V> result = runSQLFieldsQuery(filterParams);
        if (result == null || result.isEmpty())
            return null;
        if(result.size() > 1) {
            throw new CacheException(NON_SCALAR_ROWSET);
        }
        return result.get(0);
    }

    @DurationStatistics
    public <E1, E2> Map<K, V> getBySqlFieldsAll(Map<String, Object[]> filterParams) throws CacheException {
        return  runSQLFieldsQuery(filterParams);
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

    @DurationStatistics
    private String buildSqlFieldsQuery(Map<String, Object[]> filterParams) {
        StringBuilder sb = new StringBuilder("select _key, _val from " + SQL_SCHEMA_NAME +
                "." + cache.getConfiguration().getQueryEntities()[0].getTableName() + " where ");
        boolean needAnd = false;
        for (String c : filterParams.keySet()) {
            if (!sqlFields.get(c).isIndexed)
                continue;
            SqlField f = sqlFields.get(c);
            if (needAnd)
                sb.append(" and ");
            needAnd = true;

            sb.append(c).append(" = ");
            boolean needQuotes = f.type.equals("java.lang.String");
                sb.append("'");
            sb.append(filterParams.get(c)[0]);
            if (needQuotes)
                sb.append("'");
        }

        return sb.toString();
    }

    /**
     *
     * @param igniteClient
     * @param className
     * If the class has QuerySqlField annotations, add query entry fields and indexes.
     */
    private ClientCache<K, V> getOrCreateIndexedCache(IgniteClient igniteClient, String className, ClientCacheConfiguration cacheConfig, ExpiryPolicy ep) {
        String cacheName = CommonUtil.getSqlNameFromCacheName(cacheConfig.getName());
        cacheConfig.setSqlIndexMaxInlineSize(SQL_INDEX_MAX_INLINE_SIZE);

        logger.log(Level.INFO, "Creating cache " + cacheName + " with index");

        Class<?> v;
        try {
            v = Class.forName(className);
        } catch (Exception e) {
            logger.log(Level.INFO, "Failed to get class for " + className);
            return null;
        }

        QueryEntity qryEnt = new QueryEntity().setTableName(cacheName);
        qryEnt.setValueType(className);

        LinkedHashMap<String, String> qryFields = new LinkedHashMap<>();
        ArrayList<QueryIndex> idxFields = new ArrayList<>();
        try {
            for (Map.Entry<String, SqlField> e : sqlFields.entrySet()) {
                String cname = e.getKey();
                SqlField f = e.getValue();
                qryFields.put(cname, f.type);
                if (f.isIndexed) {
                    idxFields.add(new QueryIndex(cname));
                }
            }

            qryEnt.setFields(qryFields);
            qryEnt.setIndexes(idxFields);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create index on cache: " + cacheName + ": " + e.getMessage());
            return null;
        }

        cacheConfig.setQueryEntities(qryEnt);
        cacheConfig.setSqlSchema(SQL_SCHEMA_NAME);

        // also make not of this cache, somewhere, somehow?
        // have a static cache?
        ClientCache<K, V> cache;

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
        LinkedHashMap<String, SqlField> localFields = new LinkedHashMap<>();
        logger.log(Level.INFO, "Checking for QuerySqlField annotations: " + className);
        Field[] fields = null;
        try {
            Class<?> v = Class.forName(className);

            fields = ControllerUtil.getAllDeclaredFields(v);

            for (Field f : fields) {
                QuerySqlField annot = f.getAnnotation(QuerySqlField.class);
                if (annot != null) {
                    if (annot.index()) {
                            SqlField sqlField = new SqlField();
                            sqlField.isIndexed = true;
                            sqlField.type = f.getType().getTypeName();
                            localFields.put(f.getName(), sqlField);
                    } else {
                        SqlField sqlField = new SqlField();
                        sqlField.isIndexed = false;
                        sqlField.type = f.getType().getTypeName();
                        localFields.put(f.getName(), sqlField);
                    }
                }
            }

            sqlFields = localFields;
            logger.log(Level.INFO, "QuerySqlField Found " + sqlFields.size() + " sqlFields");
        } catch (Exception e) {
            logger.log(Level.INFO, "Failed to get Class info for class " + className + ", or declared fields");
        }
    }
}