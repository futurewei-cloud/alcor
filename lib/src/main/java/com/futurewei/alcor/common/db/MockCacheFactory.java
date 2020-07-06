package com.futurewei.alcor.common.db;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Profile("AclorTest")
public class MockCacheFactory implements ICacheFactory {

    private Map<Class<?>, ICache> cacheMap;

    public MockCacheFactory() {
        cacheMap = new HashMap<>();
    }

    public <K, V> ICache<K, V> getCache(Class<V> v) {
        ICache<K, V> result = cacheMap.get(v);
        if (result == null) {
            result = new MockCache<K, V>();
            cacheMap.put(v, result);
        }

        return result;
    }

    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        return this.getCache(v);
    }

    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit){
        return this.getCache(v);
    }
}
