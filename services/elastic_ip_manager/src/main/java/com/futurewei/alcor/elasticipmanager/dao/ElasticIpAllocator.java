/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.elasticipmanager.dao;

import com.futurewei.alcor.common.db.*;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAllocatedIpv4;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAllocatedIpv6;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAvailableBucketsSet;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpExistsException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpParameterException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpRangeInUseException;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;


@ComponentScan(value="com.futurewei.alcor.common.db")
@Repository
public class ElasticIpAllocator {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpAllocator.class);
    public static final int IPv4_BUCKETS_COUNT = 256;
    private static final int IPV4_ALLOCATION_MAX_RETRY_COUNT = 10;
    private static final int IPV6_ALLOCATION_MAX_RETRY_COUNT = 2000;
    public static final BigInteger EIGHT_BYTES_SCOPE_RANGE = BigInteger.valueOf(Long.MAX_VALUE).multiply(
            BigInteger.valueOf(2)).add(BigInteger.valueOf(2));

    private final ICache<String, ElasticIpAllocatedIpv4> allocatedIpv4Cache;
    private final ICache<String, ElasticIpAllocatedIpv6> allocatedIpv6Cache;
    private final ICache<String, ElasticIpAvailableBucketsSet> availableBucketsCache;
    private final IDistributedLock allocatedIpv4Lock;
    private final IDistributedLock allocatedIpv6Lock;
    private final IDistributedLock availableBucketsLock;

    @Autowired
    public ElasticIpAllocator(CacheFactory cacheFactor, DistributedLockFactory lockFactory) {
        allocatedIpv4Cache = cacheFactor.getCache(ElasticIpAllocatedIpv4.class);
        allocatedIpv6Cache = cacheFactor.getCache(ElasticIpAllocatedIpv6.class);
        availableBucketsCache = cacheFactor.getCache(ElasticIpAvailableBucketsSet.class);
        allocatedIpv4Lock = lockFactory.getDistributedLock(ElasticIpAllocatedIpv4.class);
        allocatedIpv6Lock = lockFactory.getDistributedLock(ElasticIpAllocatedIpv6.class);
        availableBucketsLock = lockFactory.getDistributedLock(ElasticIpAvailableBucketsSet.class);
    }

    private int getNextAvailableBucket(BitSet availableBucketsBitset, int startOffset) {
        int availableBucketIndex = availableBucketsBitset.nextSetBit(startOffset);
        if (availableBucketIndex >= IPv4_BUCKETS_COUNT) {
            availableBucketIndex = availableBucketsBitset.nextSetBit(0);
            if (availableBucketIndex >= IPv4_BUCKETS_COUNT) {
                return -1;
            }
        }

        return availableBucketIndex;
    }

    private ElasticIpAvailableBucketsSet createBucketState(String rangeId)
            throws CacheException, DistributedLockException {
        String availableBucketsKey = rangeId + "-available-buckets-ipv4";

        ElasticIpAvailableBucketsSet glance = availableBucketsCache.get(availableBucketsKey);
        if (glance == null) {
            glance = availableBucketsCache.get(availableBucketsKey);
            if (glance == null) {
                // initial the available buckets set
                BitSet initialBitset = new BitSet(IPv4_BUCKETS_COUNT);
                initialBitset.set(0, IPv4_BUCKETS_COUNT, true);
                glance = new ElasticIpAvailableBucketsSet(rangeId, initialBitset);

                availableBucketsCache.put(availableBucketsKey, glance);
            }
        }

        return glance;
    }

    private void setBucketState(String rangeId, int bucketIndex, boolean isAvailable)
            throws CacheException, DistributedLockException {
        String availableBucketsKey = rangeId + "-available-buckets-ipv4";
        // add resource lock
        availableBucketsLock.lock(availableBucketsKey);

        ElasticIpAvailableBucketsSet glance = availableBucketsCache.get(availableBucketsKey);
        if (glance == null) {
            // initial the available buckets set
            glance = this.createBucketState(rangeId);
        }

        BitSet bitSet = glance.getAvailableBucketsBitset();

        if (isAvailable) {
            bitSet.set(bucketIndex);
        } else {
            bitSet.clear(bucketIndex);
        }

        availableBucketsCache.put(availableBucketsKey, glance);

        // release resource lock
        availableBucketsLock.unlock(availableBucketsKey);
    }

    private ElasticIpAvailableBucketsSet getOrCreateBucketState(String rangeId)
            throws CacheException, DistributedLockException {
        String availableBucketsKey = rangeId + "-available-buckets-ipv4";

        ElasticIpAvailableBucketsSet glance = availableBucketsCache.get(availableBucketsKey);
        if (glance == null) {
            // add resource lock
            availableBucketsLock.lock(availableBucketsKey);
            // initial the available buckets set
            glance = this.createBucketState(rangeId);

            // release resource lock
            availableBucketsLock.unlock(availableBucketsKey);
        }

        return glance;
    }

    private String allocateIpv4Address(ElasticIpRange range, String specifiedIp) throws Exception {

        String ipAddress = null;
        if (specifiedIp != null) {
            long assignedIp =  Ipv4AddrUtil.ipv4ToLong(specifiedIp);
            int bucketIndex = (int)(assignedIp % IPv4_BUCKETS_COUNT);
            String ipv4AllocKey = range.getId() + "-ipv4-" + bucketIndex;

            try {
                // todo get global read lock

                // add resource lock
                allocatedIpv4Lock.lock(ipv4AllocKey);

                ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
                if (ipv4Alloc != null) {
                    Set<Long> availableIps = ipv4Alloc.getAvailableIps();
                    if (availableIps.contains(assignedIp)) {
                        availableIps.remove(assignedIp);
                        ipv4Alloc.getAllocatedIps().add(assignedIp);

                        allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);

                        if (availableIps.isEmpty()) {
                            this.setBucketState(range.getId(), bucketIndex, false);
                        }

                        ipAddress = Ipv4AddrUtil.longToIpv4(assignedIp);
                    } else {
                        // release lock
                        allocatedIpv4Lock.unlock(ipv4AllocKey);

                        // todo release global read lock

                        LOG.debug("The specified ip address is not within the elastic ip range");
                        throw new ElasticIpParameterException();
                    }
                }

                // release lock
                allocatedIpv4Lock.unlock(ipv4AllocKey);

                // todo release global read lock

            } catch (CacheException e) {
                e.printStackTrace();
                LOG.error("allocateIpv4Address cache exception:", e);
                return null;
            } catch (DistributedLockException e) {
                e.printStackTrace();
                LOG.error("allocateIpv4Address lock exception:", e);
                return null;
            }

        } else {

            Set<Integer> foreachedBuckets = new HashSet<>();
            int retryCount = 0;

            try {
                ElasticIpAvailableBucketsSet glance = this.getOrCreateBucketState(range.getId());
                BitSet bitSet = glance.getAvailableBucketsBitset();
                int availableBucketIndex = random.nextInt(IPv4_BUCKETS_COUNT);

                while (retryCount < IPV4_ALLOCATION_MAX_RETRY_COUNT) {
                    // todo get global read lock

                    availableBucketIndex = this.getNextAvailableBucket(bitSet, availableBucketIndex);
                    if (availableBucketIndex < 0 || foreachedBuckets.contains(availableBucketIndex)) {
                        LOG.debug("The IPv4 allocation range is full");

                        // todo release global read lock
                        return null;
                    }
                    foreachedBuckets.add(availableBucketIndex);

                    String ipv4AllocKey = range.getId() + "-ipv4-" + availableBucketIndex;
                    // add resource lock
                    allocatedIpv4Lock.lock(ipv4AllocKey);

                    ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
                    if (ipv4Alloc != null) {

                        Set<Long> availableIps = ipv4Alloc.getAvailableIps();
                        if (!availableIps.isEmpty()) {
                            Long assignedIp = availableIps.iterator().next();

                            availableIps.remove(assignedIp);
                            ipv4Alloc.getAllocatedIps().add(assignedIp);

                            allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);

                            if (availableIps.isEmpty()) {
                                this.setBucketState(range.getId(), availableBucketIndex, false);
                            }

                            ipAddress = Ipv4AddrUtil.longToIpv4(assignedIp);
                        } else {
                            LOG.debug("Concurrence conflict occurs, the IPv4 allocation bucket is full:"
                                    + availableBucketIndex);
                        }
                    } else {
                        LOG.error("The IPv4 allocation bucket is not found:" + availableBucketIndex);
                    }

                    // release lock
                    allocatedIpv4Lock.unlock(ipv4AllocKey);

                    // todo release global read lock

                    if (ipAddress != null) {
                        break;
                    }
                    retryCount += 1;
                }
            } catch (CacheException e) {
                e.printStackTrace();
                LOG.error("allocateIpv4Address cache exception:", e);
                return null;
            } catch (DistributedLockException e) {
                e.printStackTrace();
                LOG.error("allocateIpv4Address lock exception:", e);
                return null;
            }
        }

        return ipAddress;
    }

    private String allocateIpv6Address(ElasticIpRange range, String specifiedIp) throws Exception {
        String ipv6Address = null;
        String rangeId = range.getId();
        String ipv6AllocKeyPrefix = rangeId + "-ipv6-";
        List<ElasticIpRange.AllocationRange> cidrs = range.getAllocationRanges();

        try {
            // todo get global read lock

            if (specifiedIp != null) {
                boolean validCheck = false;
                BigInteger assignedIp = Ipv6AddrUtil.ipv6ToBitInt(specifiedIp);
                for (ElasticIpRange.AllocationRange cidr: cidrs) {
                    BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(cidr.getStart());
                    BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(cidr.getEnd());
                    if ((assignedIp.compareTo(start) >= 0) && (assignedIp.compareTo(end) <= 0)) {
                        validCheck = true;
                        break;
                    }
                }
                if (!validCheck) {
                    LOG.debug("The specified ipv6 address is not within the elastic ip range");

                    // todo release global read lock

                    throw new ElasticIpParameterException();
                }

                String ipv6AllocKey = rangeId + "-ipv6-" + specifiedIp;
                // add resource lock
                allocatedIpv6Lock.lock(ipv6AllocKey);

                ElasticIpAllocatedIpv6 ipv6Alloc = allocatedIpv6Cache.get(ipv6AllocKey);
                if (ipv6Alloc != null) {
                    // release lock
                    allocatedIpv6Lock.unlock(ipv6AllocKey);

                    // todo release global read lock

                    throw new ElasticIpExistsException();
                }

                ipv6Alloc = new ElasticIpAllocatedIpv6(rangeId, specifiedIp);
                allocatedIpv6Cache.put(ipv6AllocKey, ipv6Alloc);

                // release lock
                allocatedIpv6Lock.unlock(ipv6AllocKey);
            } else {
                int startOffset = random.nextInt(cidrs.size());

                ListIterator<ElasticIpRange.AllocationRange> iterator = cidrs.listIterator(startOffset);
                ElasticIpRange.AllocationRange allocation;
                while (iterator.hasNext()) {
                    allocation = iterator.next();

                    BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(allocation.getStart());
                    BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(allocation.getEnd());

                    BigInteger scopeSize = end.subtract(start).add(BigInteger.ONE);
                    if (scopeSize.compareTo(EIGHT_BYTES_SCOPE_RANGE) < 0) {
                        LOG.error("The ipv6 allocation range should not be larger than 2 ^ 64");
                    }

                    BigInteger rawOffset = BigInteger.valueOf(random.nextLong());
                    if (rawOffset.compareTo(BigInteger.ZERO) < 0) {
                        rawOffset = rawOffset.add(EIGHT_BYTES_SCOPE_RANGE);
                    }
                    rawOffset = rawOffset.mod(scopeSize);

                    BigInteger addressOffset = start.add(rawOffset);
                    BigInteger loop = addressOffset;
                    int retryCount = 0;
                    while (retryCount < IPV6_ALLOCATION_MAX_RETRY_COUNT) {
                        String ipv6AllocKey = ipv6AllocKeyPrefix + loop;
                        // add resource lock
                        allocatedIpv6Lock.lock(ipv6AllocKey);

                        ElasticIpAllocatedIpv6 ipv6Alloc1 = allocatedIpv6Cache.get(ipv6AllocKeyPrefix + loop);
                        if (ipv6Alloc1 == null) {
                            ipv6Address = Ipv6AddrUtil.bigIntToIpv6(loop);
                            ipv6Alloc1 = new ElasticIpAllocatedIpv6(rangeId, ipv6Address);
                            allocatedIpv6Cache.put(ipv6AllocKeyPrefix + loop, ipv6Alloc1);
                        }

                        // release lock
                        allocatedIpv6Lock.unlock(ipv6AllocKey);

                        if (ipv6Address != null) {
                            break;
                        }

                        retryCount += 1;
                        loop.add(BigInteger.ONE);
                        if (loop.compareTo(end) > 0) {
                            loop = start;
                        } else if (loop.equals(addressOffset)) {
                            break;
                        }
                    }

                    if (ipv6Address != null) {
                        break;
                    }
                }
            }
            // todo release global read lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("allocateIpv6Address exception:", e);
            return null;
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("allocateIpv6Address lock exception:", e);
            return null;
        }

        return ipv6Address;
    }

    public String allocateIpAddress(ElasticIpRange range, String specifiedIp) throws Exception {
        String ipAddress = null;
        if (range.getIpVersion() == IpVersion.IPV4.getVersion()) {
            ipAddress = this.allocateIpv4Address(range, specifiedIp);
        } else if (range.getIpVersion() == IpVersion.IPV6.getVersion()) {
            ipAddress = this.allocateIpv6Address(range, specifiedIp);
        }

        return ipAddress;
    }

    private void releaseIpv4Address(String rangeId, String ipAddress) {
        long address = Ipv4AddrUtil.ipv4ToLong(ipAddress);
        int bucketIndex = (int)(address % IPv4_BUCKETS_COUNT);
        String ipv4AllocKey = rangeId + "-ipv4-" + bucketIndex;
        try {
            // todo get global read lock

            // add resource lock
            allocatedIpv4Lock.lock(ipv4AllocKey);

            ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
            if (ipv4Alloc != null) {
                Set<Long> allocatedIps = ipv4Alloc.getAllocatedIps();
                if (allocatedIps.contains(address)) {
                    allocatedIps.remove(address);

                    Set<Long> availableIps = ipv4Alloc.getAvailableIps();
                    availableIps.add(address);

                    // update allocated ipv4 cache
                    allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);

                    if (availableIps.size() == 1) {
                        this.setBucketState(rangeId, bucketIndex, true);
                    }
                }
            }

            // release lock
            allocatedIpv4Lock.unlock(ipv4AllocKey);

            // todo release global read lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("releaseIpv4Address cache exception:", e);
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("releaseIpv4Address lock exception:", e);
        }
    }

    private void releaseIpv6Address(String rangeId, String ipAddress) {
        BigInteger address = Ipv6AddrUtil.ipv6ToBitInt(ipAddress);
        String ipv6AllocKey = rangeId + "-ipv6-" + address;
        try {
            // todo get global read lock

            // add resource lock
            allocatedIpv6Lock.lock(ipv6AllocKey);

            ElasticIpAllocatedIpv6 ipv6Alloc = allocatedIpv6Cache.get(ipv6AllocKey);
            if (ipv6Alloc != null) {
                allocatedIpv6Cache.remove(ipv6AllocKey);
            }

            // release lock
            allocatedIpv6Lock.unlock(ipv6AllocKey);

            // todo release global read lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("releaseIpv6Address cache exception:", e);
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("releaseIpv6Address lock exception:", e);
        }
    }

    public void releaseIpAddress(String rangeId, Integer ipVersion, String ipAddress) {
        if (ipVersion == IpVersion.IPV4.getVersion()) {
            this.releaseIpv4Address(rangeId, ipAddress);
        } else if (ipVersion == IpVersion.IPV6.getVersion()) {
            this.releaseIpv6Address(rangeId, ipAddress);
        }
    }

    private void elasticIpv4RangeUpdate(String rangeId, List<ElasticIpRange.AllocationRange> pools)
            throws ElasticIpRangeInUseException {

        Map<String, Set<Long>> newPools = new HashMap<>();
        String ipv4AllocKey;
        Set<Long> poolLoop;
        for (ElasticIpRange.AllocationRange rangeItem: pools) {
            long start = Ipv4AddrUtil.ipv4ToLong(rangeItem.getStart());
            long end = Ipv4AddrUtil.ipv4ToLong(rangeItem.getEnd());

            for (long loop = start; loop <= end; loop++) {
                int bucketIndex = (int)(loop % IPv4_BUCKETS_COUNT);
                ipv4AllocKey = rangeId + "-ipv4-" + bucketIndex;
                poolLoop = newPools.computeIfAbsent(ipv4AllocKey, k -> new HashSet<>());
                poolLoop.add(loop);
            }
        }

        try {
            // todo get global write lock

            Map<String, ElasticIpAllocatedIpv4> oldPoolInfos = new HashMap<>();
            for (int loop = 0; loop < IPv4_BUCKETS_COUNT; loop++) {
                ipv4AllocKey = rangeId + "-ipv4-" + loop;

                ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
                if (ipv4Alloc != null) {
                    oldPoolInfos.put(ipv4AllocKey, ipv4Alloc);
                }
            }

            // valid check and get new available ips
            boolean valid = true;
            for (ElasticIpAllocatedIpv4 poolInfoItem: oldPoolInfos.values()) {
                Set<Long> allocatedIps = poolInfoItem.getAllocatedIps();

                ipv4AllocKey = rangeId + "-ipv4-" + poolInfoItem.getIndexId();
                poolLoop = newPools.get(ipv4AllocKey);
                if (poolLoop != null) {
                    if (poolLoop.containsAll(allocatedIps)) {
                        poolLoop.removeAll(allocatedIps);
                    } else {
                        valid = false;
                    }
                } else if (!allocatedIps.isEmpty()) {
                    valid = false;
                }

                if (!valid) {
                    // todo release global write lock

                    throw new ElasticIpRangeInUseException();
                }
            }

            // update available ips
            String availableBucketsKey = rangeId + "-available-buckets-ipv4";
            ElasticIpAvailableBucketsSet glance = availableBucketsCache.get(availableBucketsKey);
            if (glance == null) {
                // initial the available buckets set
                glance = this.createBucketState(rangeId);
            }
            BitSet availableBucketSet = glance.getAvailableBucketsBitset();

            ElasticIpAllocatedIpv4 poolInfoLoop;
            for (int loop = 0; loop < IPv4_BUCKETS_COUNT; loop++) {
                ipv4AllocKey = rangeId + "-ipv4-" + loop;
                poolLoop = newPools.get(ipv4AllocKey);
                poolInfoLoop = oldPoolInfos.get(ipv4AllocKey);
                if (poolInfoLoop == null) {
                    poolInfoLoop = new ElasticIpAllocatedIpv4(rangeId, loop);
                    if (poolLoop != null) {
                        poolInfoLoop.setAvailableIps(poolLoop);
                    }
                } else {
                    if (poolLoop != null) {
                        poolInfoLoop.setAvailableIps(poolLoop);
                    } else {
                        poolInfoLoop.setAvailableIps(new HashSet<Long>());
                    }
                }

                allocatedIpv4Cache.put(ipv4AllocKey, poolInfoLoop);

                if (!poolInfoLoop.getAvailableIps().isEmpty()) {
                    availableBucketSet.set(loop);
                }
            }

            availableBucketsCache.put(availableBucketsKey, glance);

            // todo release global write lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("elasticIpRangedUpdate cache exception:", e);
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("elasticIpRangedUpdate lock exception:", e);
        }
    }

    private void elasticIpv6RangeUpdate(String rangeId, List<ElasticIpRange.AllocationRange> pools)
            throws ElasticIpRangeInUseException {

        try {
            // todo get global write lock

            Map<String, ElasticIpAllocatedIpv6> allocatedIps = allocatedIpv6Cache.getAll();
            BigInteger addressLoop;
            BigInteger start;
            BigInteger end;
            for (ElasticIpAllocatedIpv6 allocatedIpv6: allocatedIps.values()) {
                addressLoop = Ipv6AddrUtil.ipv6ToBitInt(allocatedIpv6.getAllocatedIpv6());
                boolean valid = false;
                for (ElasticIpRange.AllocationRange rangeLoop: pools) {
                    start = Ipv6AddrUtil.ipv6ToBitInt(rangeLoop.getStart());
                    end = Ipv6AddrUtil.ipv6ToBitInt(rangeLoop.getEnd());
                    if (addressLoop.compareTo(start) >= 0 && addressLoop.compareTo(end) <= 0) {
                        valid = true;
                    }
                }
                if (!valid) {
                    // todo release global write lock

                    throw new ElasticIpRangeInUseException();
                }
            }

            // todo release global write lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("allocateIpv6Address exception:", e);
        }
    }

    public void elasticIpRangedUpdate(String rangeId, Integer ipVersion,
                                      List<ElasticIpRange.AllocationRange> allocationRanges)
            throws ElasticIpRangeInUseException {

        if (ipVersion == IpVersion.IPV4.getVersion()) {
            this.elasticIpv4RangeUpdate(rangeId, allocationRanges);
        } else if (ipVersion == IpVersion.IPV6.getVersion()) {
            this.elasticIpv6RangeUpdate(rangeId, allocationRanges);
        }
    }

    private void elasticIpv4RangeDelete(String rangeId) throws ElasticIpRangeInUseException {

        try {
            // todo get global write lock

            String ipv4AllocKey;
            for (int loop = 0; loop < IPv4_BUCKETS_COUNT; loop++) {
                ipv4AllocKey = rangeId + "-ipv4-" + loop;

                ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
                if (ipv4Alloc != null && !ipv4Alloc.getAllocatedIps().isEmpty()) {
                    // todo release global write lock

                    throw new ElasticIpRangeInUseException();
                }
            }

            // todo release global write lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("elasticIpv4RangeDelete cache exception:", e);
        }
    }

    private void elasticIpv6RangeDelete(String rangeId) throws ElasticIpRangeInUseException {
        try {
            // todo get global write lock

            Map<String, ElasticIpAllocatedIpv6> allocatedIps = allocatedIpv6Cache.getAll();
            for (ElasticIpAllocatedIpv6 allocatedIpv6: allocatedIps.values()) {
                if (allocatedIpv6.getRangeId().equals(rangeId)) {
                    // todo release global write lock

                    throw new ElasticIpRangeInUseException();
                }
            }

            // todo release global write lock

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("elasticIpv6RangeDelete cache exception:", e);
        }

    }

    public void elasticIpRangedDelete(String rangeId, Integer ipVersion) throws ElasticIpRangeInUseException {
        if (ipVersion == IpVersion.IPV4.getVersion()) {
            this.elasticIpv4RangeDelete(rangeId);
        } else if (ipVersion == IpVersion.IPV6.getVersion()) {
            this.elasticIpv6RangeDelete(rangeId);
        }
    }

}
