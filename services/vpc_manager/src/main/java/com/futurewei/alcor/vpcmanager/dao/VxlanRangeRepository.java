package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.exception.InternalDbOperationException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeExistException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.entity.KeyAlloc;
import com.futurewei.alcor.vpcmanager.entity.NetworkRangeRequest;
import com.futurewei.alcor.vpcmanager.entity.NetworkVxlanRange;
import com.futurewei.alcor.vpcmanager.exception.RangeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Repository
public class VxlanRangeRepository implements ICacheRepository<NetworkVxlanRange> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ICache<String, NetworkVxlanRange> getCache() {
        return cache;
    }

    private ICache<String, NetworkVxlanRange> cache;


    @Autowired
    public VxlanRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkVxlanRange.class);
    }

    @PostConstruct
    private void init() {
        logger.info( "VxlanRangeRepository init completed");
    }


    @Override
    @DurationStatistics
    public NetworkVxlanRange findItem(String id) {
        try {
            return cache.get(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VxlanRangeRepository findItem() exception:", e);
        }
        return null;
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkVxlanRange> findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VxlanRangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkVxlanRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(NetworkVxlanRange networkVxlanRange) {
        logger.error("Add networkVxlanRange:{}", networkVxlanRange);

        try {
            cache.put(networkVxlanRange.getId(), networkVxlanRange);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VxlanRangeRepository addItem() exception:", e);
        }
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) {
        logger.error("Delete rangeId:{}", id);

        try {
            cache.remove(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VxlanRangeRepository deleteItem() exception:", e);
        }
    }

    /**
     * Allocate a key from VxlanRange repository
     * @param rangeId
     * @return range key in db
     * @throws Exception Db operation or key assignment exception
     */
    @DurationStatistics
    public synchronized Long allocateVxlanKey (String rangeId) throws Exception {
        Long key;

        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVxlanRange networkVxlanRange = cache.get(rangeId);
            if (networkVxlanRange == null) {
                throw new RangeNotFoundException();
            }

            key = networkVxlanRange.allocateKey();
            if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                cache.put(networkVxlanRange.getId(), networkVxlanRange);
            }

            tx.commit();
        }

        return key;
    }

    /**
     * Release a key from VxlanRange repository
     * @param rangId
     * @param key
     * @throws Exception Range Not Found Exception
     */
    @DurationStatistics
    public synchronized void releaseVxlanKey(String rangId, Long key) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVxlanRange networkVxlanRange = cache.get(rangId);
            if (networkVxlanRange == null) {
                throw new RangeNotFoundException();
            }

            networkVxlanRange.release(key);
            cache.put(networkVxlanRange.getId(), networkVxlanRange);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized KeyAlloc getVxlanKeyAlloc(String rangeId, Long key) throws Exception {
        NetworkVxlanRange networkVxlanRange = cache.get(rangeId);
        if (networkVxlanRange == null) {
            throw new RangeNotFoundException();
        }

        return networkVxlanRange.getNetworkKey(key);
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

            NetworkVxlanRange range = new NetworkVxlanRange(request.getId(),
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
     * Delete a vxlan range from range repo
     * @param rangeId
     * @return vxlan range
     * @throws Exception Network Range Not Found Exception
     */
    @DurationStatistics
    public synchronized NetworkVxlanRange deleteRange(String rangeId) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVxlanRange networkVxlanRange = cache.get(rangeId);
            if (networkVxlanRange == null) {
                logger.warn("Delete network range failed: network range not found");
                throw new NetworkRangeNotFoundException();
            }

            cache.remove(rangeId);

            tx.commit();

            return networkVxlanRange;
        }
    }

    /**
     * Get a vxlan range from range repo
     * @param rangeId
     * @return vxlan range
     * @throws Exception
     */
    @DurationStatistics
    public synchronized NetworkVxlanRange getRange(String rangeId) throws Exception {
        return cache.get(rangeId);
    }
}
