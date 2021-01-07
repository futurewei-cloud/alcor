package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.entity.NetworkGREType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class GreRepository implements ICacheRepository<NetworkGREType> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkGREType> getCache() {
        return cache;
    }

    private ICache<String, NetworkGREType> cache;

    @Autowired
    public GreRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkGREType.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "GreRepository init completed");
    }

    @Override
    @DurationStatistics
    public NetworkGREType findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkGREType> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkGREType> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(NetworkGREType newItem) throws CacheException {
        logger.log(Level.INFO, "Add Gre, Gre Id:" + newItem.getGreId());
        cache.put(newItem.getGreId(), newItem);
    }

    @Override
    @DurationStatistics
    public void addItems(List<NetworkGREType> items) throws CacheException {
        logger.log(Level.INFO, "Add Gre Batch:" + items);
        Map<String, NetworkGREType> networkGRETypeMap = items.stream().collect(Collectors.toMap(NetworkGREType::getGreId, Function.identity()));
        cache.putAll(networkGRETypeMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete Gre, Gre Id:" + id);
        cache.remove(id);
    }

}
