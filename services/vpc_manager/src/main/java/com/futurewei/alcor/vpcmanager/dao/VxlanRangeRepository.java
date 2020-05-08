package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.web.entity.KeyAlloc;
import com.futurewei.alcor.web.entity.NetworkVlanRange;
import com.futurewei.alcor.web.entity.NetworkRangeRequest;
import com.futurewei.alcor.web.entity.NetworkVxlanRange;
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
    public void deleteItem(String id) {
        logger.error("Delete rangeId:{}", id);

        try {
            cache.remove(id);
        } catch (CacheException e) {
            e.printStackTrace();
            logger.error("VxlanRangeRepository deleteItem() exception:", e);
        }
    }

    public synchronized Long allocateVxlanKey (String rangeId) throws Exception {
        Long key;

        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVxlanRange networkVxlanRange = cache.get(rangeId);
            if (networkVxlanRange == null) {
                throw new VlanNotFoundException();
            }

            key = networkVxlanRange.allocateKey();
            cache.put(networkVxlanRange.getId(), networkVxlanRange);

            tx.commit();
        }

        return key;
    }

    public synchronized void releaseVxlanKey(String rangId, Long key) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVxlanRange networkVxlanRange = cache.get(rangId);
            if (networkVxlanRange == null) {
                throw new VlanNotFoundException();
            }

            networkVxlanRange.release(key);
            cache.put(networkVxlanRange.getId(), networkVxlanRange);

            tx.commit();
        }
    }

    public synchronized KeyAlloc getVlanKeyAlloc(String rangeId, Long key) throws Exception {
        NetworkVxlanRange networkVxlanRange = cache.get(rangeId);
        if (networkVxlanRange == null) {
            throw new VlanNotFoundException();
        }

        return networkVxlanRange.getNetworkKey(key);
    }

    public synchronized void createRange(NetworkRangeRequest request) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            if (cache.get(request.getId()) != null) {
                logger.warn("Create network range failed: Network Range already exists");
                throw new NetworkRangeExistException();
            }

            NetworkVxlanRange range = new NetworkVxlanRange(request.getId(), request.getSegmentId(),
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

    }

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

    public synchronized NetworkVxlanRange getRange(String rangeId) throws Exception {
        return cache.get(rangeId);
    }
}
