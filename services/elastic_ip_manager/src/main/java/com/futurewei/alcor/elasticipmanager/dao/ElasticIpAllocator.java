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
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpExistsException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpParameterException;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


@ComponentScan(value="com.futurewei.alcor.common.db")
@Repository
public class ElasticIpAllocator {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpAllocator.class);
    public static final int IPv4_ALLOCATION_DEVISOR = 100;
    public static final BigInteger EIGHT_BYTES_SCOPE_MASK = BigInteger.valueOf(Long.MAX_VALUE).multiply(
            BigInteger.valueOf(2)).add(BigInteger.valueOf(1));

    private ICache<String, ElasticIpAllocatedIpv4> allocatedIpv4Cache;
    private ICache<String, ElasticIpAllocatedIpv6> allocatedIpv6Cache;
    private IDistributedLock allocatedIpv4Lock;
    private IDistributedLock allocatedIpv6Lock;

    @Autowired
    public ElasticIpAllocator(CacheFactory cacheFactor, DistributedLockFactory lockFactory) {
        allocatedIpv4Cache = cacheFactor.getCache(ElasticIpAllocatedIpv4.class);
        allocatedIpv6Cache = cacheFactor.getCache(ElasticIpAllocatedIpv6.class);
        allocatedIpv4Lock = lockFactory.getDistributedLock(ElasticIpAllocatedIpv4.class);
        allocatedIpv6Lock = lockFactory.getDistributedLock(ElasticIpAllocatedIpv6.class);
    }

    private String allocateIpv4Loop(long start, long end, Set<Long> allocatedIps)
            throws CacheException, DistributedLockException{
        String ipv4Address = null;
        for (long i = start; i < end; i += IPv4_ALLOCATION_DEVISOR) {
            if (!allocatedIps.contains(i)) {
                allocatedIps.add(i);
                ipv4Address = Ipv4AddrUtil.longToIpv4(i);
                break;
            }
        }
        return ipv4Address;
    }

    private String tryAllocateOneIpv4Address(String rangeId, int tail, long start, long end) {
        String ipv4Address = null;
        String ipv4AllocKey = rangeId + "-ipv4-" + tail;
        try {
            // add resource lock
            allocatedIpv4Lock.lock(ipv4AllocKey);

            ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
            if (ipv4Alloc == null) {
                ipv4Alloc = new ElasticIpAllocatedIpv4(rangeId, tail);
                allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);
            }

            long scopeSize = (end - start) + 1;
            Assert.isTrue(scopeSize <= Integer.MAX_VALUE, "The ipv4 allocation range should " +
                    "not be larger than 0x7fffffff");
            int offset = random.nextInt((int)scopeSize);
            long adjustedOffset = offset + (IPv4_ALLOCATION_DEVISOR - ((start + offset) % IPv4_ALLOCATION_DEVISOR))
                    + tail;
            Set<Long> allocatedIps = ipv4Alloc.getAllocatedIps();
            ipv4Address = allocateIpv4Loop(start + adjustedOffset, end+1, allocatedIps);
            if (ipv4Address == null) {
                ipv4Address = allocateIpv4Loop(start, adjustedOffset, allocatedIps);
            }

            if (ipv4Address != null) {
                // update allocated ipv4 cache
                allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);
            }

            // release lock
            allocatedIpv4Lock.unlock(ipv4AllocKey);

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("tryAllocateOneIpv4Address cache exception:", e);
            return null;
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("tryAllocateOneIpv4Address lock exception:", e);
            return null;
        }

        return ipv4Address;
    }

    private String tryAllocateTheSpecifiedIpv4Address(String rangeId, long start, long end, String specifiedIp
    ) throws ElasticIpExistsException {
        long address = Ipv4AddrUtil.ipv4ToLong(specifiedIp);
        int tail = (int)address % IPv4_ALLOCATION_DEVISOR;
        String ipv4Address = null;
        String ipv4AllocKey = rangeId + "-ipv4-" + tail;
        try {
            // add resource lock
            allocatedIpv4Lock.lock(ipv4AllocKey);

            ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
            if (ipv4Alloc == null) {
                ipv4Alloc = new ElasticIpAllocatedIpv4(rangeId, tail);
                allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);
            }
            Set<Long> allocatedIps = ipv4Alloc.getAllocatedIps();
            if (allocatedIps.contains(address)) {
                // release lock
                allocatedIpv4Lock.unlock(ipv4AllocKey);
                throw new ElasticIpExistsException();
            }
            allocatedIps.add(address);
            ipv4Address = Ipv4AddrUtil.longToIpv4(address);
            // update allocated ipv4 cache
            allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);

            // release lock
            allocatedIpv4Lock.unlock(ipv4AllocKey);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("tryAllocateTheSpecifiedIpv4Address cache exception:", e);
            return null;
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("tryAllocateTheSpecifiedIpv4Address lock exception:", e);
            return null;
        }

        return ipv4Address;
    }

    public String allocateIpv4Address(ElasticIpRange range, String specifiedIp) throws Exception {

        String ipAddress = null;
        List<ElasticIpRange.AllocationRange> cidrs = range.getAllocationRanges();
        if (specifiedIp != null) {
            for (ElasticIpRange.AllocationRange cidr: cidrs) {
                long start = Ipv4AddrUtil.ipv4ToLong(cidr.getStart());
                long end = Ipv4AddrUtil.ipv4ToLong(cidr.getEnd());
                ipAddress = tryAllocateTheSpecifiedIpv4Address(range.getId(), start, end, specifiedIp);
                if (ipAddress != null) {
                    break;
                }
            }
            if (ipAddress == null) {
                LOG.debug("The specified ip address is not within the elastic ip range");
                throw new ElasticIpParameterException();
            }
        } else {

            int startOffset = random.nextInt(cidrs.size());
            List<ElasticIpRange.AllocationRange> sortedCidrs = new ArrayList<>();
            for (int i = startOffset; i < cidrs.size(); i++) {
                sortedCidrs.add(cidrs.get(i));
            }
            for (int i  =0; i < startOffset; i++) {
                sortedCidrs.add(cidrs.get(i));
            }

            for (ElasticIpRange.AllocationRange allocation: sortedCidrs ) {
                long start = Ipv4AddrUtil.ipv4ToLong(allocation.getStart());
                long end = Ipv4AddrUtil.ipv4ToLong(allocation.getEnd());

                int offset = random.nextInt(IPv4_ALLOCATION_DEVISOR);
                for (int i = offset; i < IPv4_ALLOCATION_DEVISOR; i++) {
                    ipAddress = tryAllocateOneIpv4Address(range.getId(), offset, start, end);
                    if (ipAddress != null) {
                        break;
                    }
                }
                if (ipAddress == null) {
                    for (int i = 0; i < offset; i++) {
                        ipAddress = tryAllocateOneIpv4Address(range.getId(), offset, start, end);
                        if (ipAddress != null) {
                            break;
                        }
                    }
                }
            }
        }

        return ipAddress;
    }

    private String AllocateIpv6Loop(BigInteger start, BigInteger end, String rangeId)
            throws CacheException, DistributedLockException{
        String ipv6AllocKeyPrefix = rangeId + "-ipv6-";
        String ipv6Address = null;
        for (BigInteger i = start; i.compareTo(end) < 0; i.add(BigInteger.ONE)) {
            String ipv6AllocKey = ipv6AllocKeyPrefix+ i;
            // add resource lock
            allocatedIpv6Lock.lock(ipv6AllocKey);

            ElasticIpAllocatedIpv6 ipv6Alloc = allocatedIpv6Cache.get(ipv6AllocKeyPrefix + i);
            if (ipv6Alloc == null) {
                ipv6Address = Ipv6AddrUtil.bigIntToIpv6(i);
                ipv6Alloc = new ElasticIpAllocatedIpv6(rangeId, ipv6Address);
                allocatedIpv6Cache.put(ipv6AllocKeyPrefix+ i, ipv6Alloc);

                // release lock
                allocatedIpv6Lock.unlock(ipv6AllocKey);
                break;
            }

            // release lock
            allocatedIpv6Lock.unlock(ipv6AllocKey);
        }
        return ipv6Address;
    }

    private String tryAllocateOneIpv6Address(String rangeId, BigInteger start, BigInteger end) {
        String ipv6Address = null;
        String ipv6AllocKeyPrefix = rangeId + "-ipv6-";
        try {
            BigInteger scopeSize = end.subtract(start).add(BigInteger.ONE);
            Assert.isTrue(scopeSize.compareTo(EIGHT_BYTES_SCOPE_MASK) <= 0,
                    "The ipv6 allocation range should not be larger than 0xffffffffffffffff");
            BigInteger rawOffset = BigInteger.valueOf(random.nextLong());
            if (rawOffset.compareTo(BigInteger.ZERO) < 0) {
                rawOffset = rawOffset.add(EIGHT_BYTES_SCOPE_MASK).add(BigInteger.ONE);
            }
            rawOffset = rawOffset.mod(scopeSize);

            BigInteger loopStart = start.add(rawOffset);
            ipv6Address = AllocateIpv6Loop(loopStart, end.add(BigInteger.ONE), rangeId);
            if (ipv6Address == null) {
                ipv6Address = AllocateIpv6Loop(BigInteger.ZERO, loopStart, rangeId);
            }

        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("tryAllocateIpv6Address exception:", e);
            return null;
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("tryAllocateIpv6Address lock exception:", e);
            return null;
        }

        return ipv6Address;
    }

    private String tryAllocateTheSpecifiedIpv6Address(String rangeId, BigInteger start, BigInteger end, String specifiedIp
    ) throws Exception {
        String ipv6AllocKey = rangeId + "-ipv6-" + specifiedIp;

        try {
            // add resource lock
            allocatedIpv6Lock.lock(ipv6AllocKey);

            ElasticIpAllocatedIpv6 ipv6Alloc = allocatedIpv6Cache.get(ipv6AllocKey);
            if (ipv6Alloc != null) {
                // release lock
                allocatedIpv6Lock.unlock(ipv6AllocKey);
                throw new ElasticIpExistsException();
            }

            ipv6Alloc = new ElasticIpAllocatedIpv6(rangeId, specifiedIp);
            allocatedIpv6Cache.put(ipv6AllocKey, ipv6Alloc);

            // release lock
            allocatedIpv6Lock.unlock(ipv6AllocKey);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("tryAllocateTheSpecifiedIpv6Address exception:", e);
            return null;
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("tryAllocateIpv6Address lock exception:", e);
            return null;
        }

        return specifiedIp;
    }

    public String allocateIpv6Address(ElasticIpRange range, String specifiedIp) throws Exception {
        String ipv6Address = null;
        List<ElasticIpRange.AllocationRange> cidrs = range.getAllocationRanges();

        if (specifiedIp != null) {
            for (ElasticIpRange.AllocationRange cidr: cidrs) {
                BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(cidr.getStart());
                BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(cidr.getEnd());
                ipv6Address = tryAllocateTheSpecifiedIpv6Address(range.getId(), start, end, specifiedIp);
                if (ipv6Address != null) {
                    break;
                }
            }
            if (ipv6Address == null) {
                LOG.debug("The specified ipv6 address is not within the elastic ip range");
                throw new ElasticIpParameterException();
            }
        } else {
            int startOffset = random.nextInt(cidrs.size());
            List<ElasticIpRange.AllocationRange> sortedCidrs = new ArrayList<>();
            for (int i = startOffset; i < cidrs.size(); i++) {
                sortedCidrs.add(cidrs.get(i));
            }
            for (int i  =0; i < startOffset; i++) {
                sortedCidrs.add(cidrs.get(i));
            }

            for (ElasticIpRange.AllocationRange allocation: sortedCidrs) {
                BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(allocation.getStart());
                BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(allocation.getEnd());
                ipv6Address = tryAllocateOneIpv6Address(range.getId(), start, end);
                if (ipv6Address != null) {
                    break;
                }
            }
        }

        return ipv6Address;
    }

    public void releaseIpv4Address(String rangeId, String ipAddress) {
        long address = Ipv4AddrUtil.ipv4ToLong(ipAddress);
        int tail = (int)address % IPv4_ALLOCATION_DEVISOR;
        String ipv4AllocKey = rangeId + "-ipv4-" + tail;
        try {
            // add resource lock
            allocatedIpv4Lock.lock(ipv4AllocKey);

            ElasticIpAllocatedIpv4 ipv4Alloc = allocatedIpv4Cache.get(ipv4AllocKey);
            if (ipv4Alloc == null) {
                // release lock
                allocatedIpv4Lock.unlock(ipv4AllocKey);
                return;
            }
            Set<Long> allocatedIps = ipv4Alloc.getAllocatedIps();
            if (allocatedIps.contains(address)) {
                allocatedIps.remove(address);
                if (allocatedIps.size() == 0) {
                    allocatedIpv4Cache.remove(ipv4AllocKey);
                } else {
                    // update allocated ipv4 cache
                    allocatedIpv4Cache.put(ipv4AllocKey, ipv4Alloc);
                }
            }

            // release lock
            allocatedIpv4Lock.unlock(ipv4AllocKey);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("releaseIpv4Address cache exception:", e);
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("releaseIpv4Address lock exception:", e);
        }
    }

    public void releaseIpv6Address(String rangeId, String ipAddress) {
        BigInteger address = Ipv6AddrUtil.ipv6ToBitInt(ipAddress);
        String ipv6AllocKey = rangeId + "-ipv6-" + address;
        try {
            // add resource lock
            allocatedIpv6Lock.lock(ipv6AllocKey);

            ElasticIpAllocatedIpv6 ipv6Alloc = allocatedIpv6Cache.get(ipv6AllocKey);
            if (ipv6Alloc != null) {
                allocatedIpv6Cache.remove(ipv6AllocKey);
            }

            // release lock
            allocatedIpv6Lock.unlock(ipv6AllocKey);
        } catch (CacheException e) {
            e.printStackTrace();
            LOG.error("releaseIpv6Address cache exception:", e);
        } catch (DistributedLockException e) {
            e.printStackTrace();
            LOG.error("releaseIpv6Address lock exception:", e);
        }
    }
}
