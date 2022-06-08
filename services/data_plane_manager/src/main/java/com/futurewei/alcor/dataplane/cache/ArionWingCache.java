package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.entity.ArionWing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class ArionWingCache {

    private ICache<String, ArionWing> arionWingCache;
    private ICache<String, Object> arionWingGroupCache;
    private CacheFactory cacheFactory;

    @Autowired
    public ArionWingCache(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        arionWingCache = cacheFactory.getCache(ArionWing.class);
        arionWingGroupCache = cacheFactory.getCache(Object.class);
    }

    @DurationStatistics
    public ArionWing getArionWing (String resourceId) throws CacheException {
        return arionWingCache.get(resourceId);
    }

    @DurationStatistics
    public Collection<ArionWing> getArionWings () throws CacheException {
        return arionWingCache.getAll().values();
    }

    @DurationStatistics
    public Map<String, ArionWing> getAllSubnetPorts(Map<String, Object[]> queryParams) throws CacheException {
        return arionWingCache.getAll(queryParams);
    }

    @DurationStatistics
    public Collection<ArionWing> getArionWings (Set<String> keys) throws CacheException {
        return arionWingCache.getAll(keys).values();
    }

    @DurationStatistics
    public void insertArionWing (ArionWing arionWing) throws CacheException {
        arionWingCache.put(String.valueOf(arionWing.hashCode()), arionWing);
    }

    @DurationStatistics
    public void deleteArionWing (String resourceId) throws CacheException {
        arionWingCache.remove(resourceId);
    }

    @DurationStatistics
    public Object getArionGroup (String resourceId) throws CacheException {
        return arionWingGroupCache.get(resourceId);
    }

    @DurationStatistics
    public void insertArionGroup (String resourceId) throws CacheException {
        System.out.println("Insert arion group: " + resourceId);
        arionWingGroupCache.put(resourceId, resourceId);
    }

    @DurationStatistics
    public void deleteArionGroup (String resourceId) throws CacheException {
        arionWingGroupCache.remove(resourceId);
    }

    @DurationStatistics
    public Map<String, Object> getAllArionGroup () throws CacheException {
        return arionWingGroupCache.getAll();
    }

}
