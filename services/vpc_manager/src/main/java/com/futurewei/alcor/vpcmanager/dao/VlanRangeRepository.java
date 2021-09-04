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
import com.futurewei.alcor.vpcmanager.entity.NetworkVlanRange;
import com.futurewei.alcor.vpcmanager.exception.InternalDbOperationException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeExistException;
import com.futurewei.alcor.vpcmanager.exception.NetworkRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.exception.RangeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
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
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
    public Map<String, NetworkVlanRange> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
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
    @DurationStatistics
    public synchronized void addItems(List<NetworkVlanRange> items) throws CacheException {
        Map<String, NetworkVlanRange> networkVlanRangeMap = items.stream().collect(Collectors.toMap(NetworkVlanRange::getId, Function.identity()));
        cache.putAll(networkVlanRangeMap);
    }

    @Override
    @DurationStatistics
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
    @DurationStatistics
    public synchronized Long allocateVlanKey (String rangeId) throws Exception {
        Long key;

        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVlanRange networkVlanRange = cache.get(rangeId);
            if (networkVlanRange == null) {
                throw new RangeNotFoundException();
            }

            key = networkVlanRange.allocateKey();
            if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                cache.put(networkVlanRange.getId(), networkVlanRange);
            }

            tx.commit();
        }

        return key;
    }

    /**
     * Release a key from VlanRange repository
     * @param rangId
     * @param key
     * @throws Exception Range Not Found Exception
     */
    @DurationStatistics
    public synchronized void releaseVlanKey(String rangId, Long key) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            NetworkVlanRange networkVlanRange = cache.get(rangId);
            if (networkVlanRange == null) {
                throw new RangeNotFoundException();
            }

            networkVlanRange.release(key);
            cache.put(networkVlanRange.getId(), networkVlanRange);

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized KeyAlloc getVlanKeyAlloc(String rangeId, Long key) throws Exception {
        NetworkVlanRange networkVlanRange = cache.get(rangeId);
        if (networkVlanRange == null) {
            throw new RangeNotFoundException();
        }

        return networkVlanRange.getNetworkKey(key);
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

            NetworkVlanRange range = new NetworkVlanRange(request.getId(),
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
        return request.getId();
    }

    /**
     * Delete a vlan range from range repo
     * @param rangeId
     * @return vlan range
     * @throws Exception Network Range Not Found Exception
     */
    @DurationStatistics
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

    /**
     * Get a vlan range from range repo
     * @param rangeId
     * @return vlan range
     * @throws Exception
     */
    @DurationStatistics
    public synchronized NetworkVlanRange getRange(String rangeId) throws Exception {
        return cache.get(rangeId);
    }

    @Override
    @DurationStatistics
    public Set<String> getSubnetIds(String vpcId) throws CacheException {
        return new HashSet<>();
    }

    @Override
    @DurationStatistics
    public void addSubnetId(String vpcId, String subnetId) throws CacheException {

    }

    @Override
    @DurationStatistics
    public void deleteSubnetId(String vpcId, String subnetId) throws CacheException {

    }
}
