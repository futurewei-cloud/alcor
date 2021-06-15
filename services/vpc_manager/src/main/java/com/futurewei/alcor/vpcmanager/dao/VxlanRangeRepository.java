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
package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.entity.KeyAlloc;
import com.futurewei.alcor.vpcmanager.entity.NetworkRangeRequest;
import com.futurewei.alcor.vpcmanager.entity.NetworkVxlanRange;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void addItems(List<NetworkVxlanRange> items) throws CacheException {
        Map<String, NetworkVxlanRange> networkVxlanRangeMap = items.stream().collect(Collectors.toMap(NetworkVxlanRange::getId, Function.identity()));
        cache.putAll(networkVxlanRangeMap);
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

            networkVxlanRange.tryToRelease(key);
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
     * @param requestList
     * @throws Exception Network Range Already Exist Exception
     * @throws Exception Internal Db Operation Exception
     */
    @DurationStatistics
    public synchronized void createRange(List<NetworkRangeRequest> requestList) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            HashSet<String>  set = new HashSet<>();
            Map<String, NetworkVxlanRange> ranges = new HashMap<>();
            for (NetworkRangeRequest request : requestList)
            {
                set.add(request.getId());
                ranges.put(request.getId(), new NetworkVxlanRange(request.getId(),
                        request.getNetworkType(), request.getPartition(), request.getFirstKey(), request.getLastKey()));
            }
            if (cache.getAll(set).keySet().size() != 0) {
                logger.warn("Create network range failed: Network Range already exists");
                throw new NetworkRangeExistException();
            }
            cache.putAll(ranges);
            tx.commit();
        }
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
