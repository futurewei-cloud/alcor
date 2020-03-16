package com.futurewei.alcor.controller.db.ignite;

import com.futurewei.alcor.controller.db.ICache;
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
    private static final int QUERY_PAGE_SIZE = 1000;
    private ClientCache<K, V> cache;

    public IgniteCache(IgniteClient igniteClient, String name) {
        try {
            cache = igniteClient.getOrCreateCache(name);
        }
        catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for vpc failed:" + e.getMessage());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(igniteClient, "Create cache for vpc failed");
    }

    @Override
    public V get(K var1) {
        return cache.get(var1);
    }

    @Override
    public void put(K var1, V var2) {
        cache.put(var1, var2);
    }

    @Override
    public boolean containsKey(K var1) {
        return cache.containsKey(var1);
    }

    @Override
    public Map<K, V> getAll() {
        Query<Cache.Entry<K, V>> qry = new ScanQuery<K, V>
                ((k, v) -> k != null) .setPageSize(QUERY_PAGE_SIZE);

        QueryCursor<Cache.Entry<K, V>> cur = cache.query(qry);

        return cur.getAll().stream().collect(Collectors
                .toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> var1) {
        cache.putAll(var1);
    }

    @Override
    public boolean remove(K var1) {
        return cache.remove(var1);
    }
}
