package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.entity.KeyAlloc;
import com.futurewei.alcor.vpcmanager.entity.NetworkGRERange;
import com.futurewei.alcor.vpcmanager.entity.NetworkRangeRequest;
import com.futurewei.alcor.vpcmanager.exception.InternalDbOperationException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeExistException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.exception.RangeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class GreRangeRepository implements ICacheRepository<NetworkGRERange> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ICache<String, NetworkGRERange> getCache() {
        return cache;
    }

    private ICache<String, NetworkGRERange> cache;


    @Autowired
    public GreRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkGRERange.class);
    }

    @PostConstruct
    private void init() {
        logger.info( "GreRangeRepository init completed");
    }


    @Override
    @DurationStatistics
    public NetworkGRERange findItem(String id) {
        try {
            return cache.get(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("GreRangeRepository findItem() exception:", e);
        }
        return null;
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkGRERange> findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("GreRangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkGRERange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(NetworkGRERange networkGRERange) {
        logger.error("Add networkGreRange:{}", networkGRERange);

        try {
            cache.put(networkGRERange.getId(), networkGRERange);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("GreRangeRepository addItem() exception:", e);
        }
    }

    @Override
    @DurationStatistics
    public void addItems(List<NetworkGRERange> items) throws CacheException {
        Map<String, NetworkGRERange> networkGRERangeMap = items.stream().collect(Collectors.toMap(NetworkGRERange::getId, Function.identity()));
        cache.putAll(networkGRERangeMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) {
        logger.error("Delete rangeId:{}", id);

        try {
            cache.remove(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("GreRangeRepository deleteItem() exception:", e);
        }
    }

    /**
     * Allocate a key from GreRange repository
     * @param rangeId
     * @return range key in db
     * @throws Exception Db operation or key assignment exception
     */
    @DurationStatistics
    public synchronized Long allocateGreKey (String rangeId) throws Exception {
        Long key;

        try (Transaction tx = cache.getTransaction().start()) {
            NetworkGRERange networkGRERange = cache.get(rangeId);
            if (networkGRERange == null) {
                throw new RangeNotFoundException();
            }

            key = networkGRERange.allocateKey();
            if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                cache.put(networkGRERange.getId(), networkGRERange);
            }

            tx.commit();
        }

        return key;
    }

    /**
     * Release a key from GreRange repository
     * @param rangId
     * @param key
     * @throws Exception Range Not Found Exception
     */
    @DurationStatistics
    public synchronized void releaseGreKey(String rangId, Long key) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkGRERange networkGRERange = cache.get(rangId);
            if (networkGRERange == null) {
                throw new RangeNotFoundException();
            }

            networkGRERange.tryToRelease(key);
            cache.put(networkGRERange.getId(), networkGRERange);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized KeyAlloc getGreKeyAlloc(String rangeId, Long key) throws Exception {
        NetworkGRERange networkGRERange = cache.get(rangeId);
        if (networkGRERange == null) {
            throw new RangeNotFoundException();
        }

        return networkGRERange.getNetworkKey(key);
    }

    /**
     * Create a range entity from range repo
     * @param request
     * @throws Exception Network Range Already Exist Exception
     * @throws Exception Internal Db Operation Exception
     */
    @DurationStatistics
    public synchronized String createRange(NetworkRangeRequest request) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            if (cache.get(request.getId()) != null) {
                logger.warn("Create network range failed: Network Range already exists");
                throw new NetworkRangeExistException();
            }

            NetworkGRERange range = new NetworkGRERange(request.getId(),
                    request.getNetworkType(), request.getPartition(), request.getFirstKey(), request.getLastKey());

            cache.put(request.getId(), range);

            range = cache.get(request.getId());
            if (range == null) {
                logger.warn("Create network range failed: Internal db operation error");
                throw new InternalDbOperationException();
            }

            request.setUsedKeys(range.getUsedKeys());
            request.setTotalKeys(range.getTotalKeys());

            tx.commit();
        }
        return String.valueOf(request.getPartition());
    }

    /**
     * Delete a gre range from range repo
     * @param rangeId
     * @return gre range
     * @throws Exception Network Range Not Found Exception
     */
    @DurationStatistics
    public synchronized NetworkGRERange deleteRange(String rangeId) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkGRERange networkGRERange = cache.get(rangeId);
            if (networkGRERange == null) {
                logger.warn("Delete network range failed: network range not found");
                throw new NetworkRangeNotFoundException();
            }

            cache.remove(rangeId);

            tx.commit();

            return networkGRERange;
        }
    }

    /**
     * Get a gre range from range repo
     * @param rangeId
     * @return gre range
     * @throws Exception
     */
    @DurationStatistics
    public synchronized NetworkGRERange getRange(String rangeId) throws Exception {
        return cache.get(rangeId);
    }
}
