package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.web.allocator.VlanKeyAllocator;
import com.futurewei.alcor.web.entity.NetworkVlanRange;
import com.futurewei.alcor.web.entity.NetworkVlanRangeRequest;
import com.futurewei.alcor.web.entity.NetworkVlanType;
import com.futurewei.alcor.web.entity.VlanKeyAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class VlanRangeRepository implements ICacheRepository<NetworkVlanRange> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ICache<String, NetworkVlanRange> getCache() {
        return cache;
    }

    private ICache<String, NetworkVlanRange> cache;


    @Autowired
    public VlanRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkVlanRange.class);
    }

    @PostConstruct
    private void init() {
        logger.info( "VlanRangeRepository init completed");
    }

    @Override
    public synchronized NetworkVlanRange findItem(String id) {
        try {
            return cache.get(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VlanRangeRepository findItem() exception:", e);
        }
        return null;
    }

    @Override
    public synchronized Map<String, NetworkVlanRange> findAllItems() {
        try {
            return cache.getAll();
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VlanRangeRepository findAllItems() exception:", e);
        }

        return new HashMap();
    }

    @Override
    public synchronized void addItem(NetworkVlanRange networkVlanRange){
        logger.error("Add networkVlanRange:{}", networkVlanRange);

        try {
            cache.put(networkVlanRange.getId(), networkVlanRange);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VlanRangeRepository addItem() exception:", e);
        }
    }

    @Override
    public synchronized void deleteItem(String id) {
        logger.error("Delete rangeId:{}", id);

        try {
            cache.remove(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VlanRangeRepository deleteItem() exception:", e);
        }
    }

    /**
     * Allocate a key from VlanRange repository
     * @param rangeId
     * @return range key in db
     * @throws Exception Db operation or key assignment exception
     */
    public synchronized Long allocateVlanKey (String rangeId) throws Exception {
        Long key;

        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVlanRange networkVlanRange = cache.get(rangeId);
            if (networkVlanRange == null) {
                throw new VlanNotFoundException();
            }

            key = networkVlanRange.allocateVlan();
            cache.put(networkVlanRange.getId(), networkVlanRange);

            tx.commit();
        }

        return key;
    }

    public synchronized void releaseVlanKey(String rangId, Long key) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVlanRange networkVlanRange = cache.get(rangId);
            if (networkVlanRange == null) {
                throw new VlanNotFoundException();
            }

            networkVlanRange.release(key);
            cache.put(networkVlanRange.getId(), networkVlanRange);

            tx.commit();
        }
    }

    public synchronized VlanKeyAlloc getVlanKeyAlloc(String rangeId, Long key) throws Exception {
        NetworkVlanRange networkVlanRange = cache.get(rangeId);
        if (networkVlanRange == null) {
            throw new VlanNotFoundException();
        }

        return networkVlanRange.getVlanKey(key);
    }

    public synchronized void createRange(NetworkVlanRangeRequest request) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            if (cache.get(request.getId()) != null) {
                logger.warn("Create network range failed: Network Range already exists");
                throw new NetworkRangeExistException();
            }

            NetworkVlanRange range = new NetworkVlanRange(request.getId(), request.getSegmentId(),
                    request.getNetworkType(), request.getFirstKey(), request.getLastKey());

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

    }

    public synchronized NetworkVlanRange deleteRange(String rangeId) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVlanRange networkVlanRange = cache.get(rangeId);
            if (networkVlanRange == null) {
                logger.warn("Delete network range failed: network range not found");
                throw new NetworkRangeNotFoundException();
            }

            cache.remove(rangeId);

            tx.commit();

            return networkVlanRange;
        }
    }

    public synchronized NetworkVlanRange getRange(String rangeId) throws Exception {
        return cache.get(rangeId);
    }
}
