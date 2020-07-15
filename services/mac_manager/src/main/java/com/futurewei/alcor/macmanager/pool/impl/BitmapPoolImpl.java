/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.macmanager.pool.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.DistributedLockFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.macmanager.dao.MacRangeMappingRepository;
import com.futurewei.alcor.macmanager.dao.MacRangePartitionRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import com.futurewei.alcor.macmanager.exception.MacAddressFullException;
import com.futurewei.alcor.macmanager.exception.MacAddressRetryLimitExceedException;
import com.futurewei.alcor.macmanager.exception.MacRepositoryTransactionErrorException;
import com.futurewei.alcor.macmanager.pool.MacPoolApi;
import com.futurewei.alcor.macmanager.utils.MacManagerConstant;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacRangePartition;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import static com.futurewei.alcor.macmanager.utils.MacUtils.*;

//@Component
public class BitmapPoolImpl implements MacPoolApi {

    private static final Logger logger = LoggerFactory.getLogger(BitmapPoolImpl.class);

    private static final int PARTITION_SIZE = 4096;

    @Value("${macmanager.retrylimit}")
    private long nRetryLimit;

    @Autowired
    private MacRangePartitionRepository macRangePartitionRepository;

    @Autowired
    private MacRangeMappingRepository macRangeMappingRepository;

    private final IDistributedLock lock;

    @Autowired
    public BitmapPoolImpl(DistributedLockFactory cacheFactory){
        lock = cacheFactory.getDistributedLock(MacRangePartition.class);
    }

    @Override
    public String allocate(String oui, MacRange macRange) throws MacAddressFullException,
            MacAddressRetryLimitExceedException, CacheException {
        String rangeId = macRange.getRangeId();
        long used = macRangeMappingRepository.size(rangeId);
        if(used >= macRange.getCapacity()){
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        }

        int partitionNubs = (int) (macRange.getCapacity()/PARTITION_SIZE + 1);
        int partitionIndex = 0;
        int retryTime = 0;
        while (retryTime < nRetryLimit) {

            boolean locked = false;
            while (!locked) {
                partitionIndex = RandomUtils.nextInt(0, partitionNubs);
                locked = lock.tryLock(rangeId + "_" + partitionIndex);
            }

            String id = rangeId + "_" + partitionIndex;
            try {
                boolean needUpdate = false;
                MacRangePartition macRangePartition = macRangePartitionRepository.findItem(id);

                // if null create a new one
                if (macRangePartition == null) {
                    macRangePartition = new MacRangePartition(id, partitionIndex, PARTITION_SIZE * partitionIndex,
                            PARTITION_SIZE * (partitionIndex + 1));
                    needUpdate = true;
                }

                while (macRangePartition.getUsed() < macRangePartition.getTotal()) {
                    long macLong = generate(macRangePartition);
                    if(check(rangeId, macLong)) {
                        macRangePartition.incUsed();
                        macRangePartitionRepository.addItem(macRangePartition);
                        return longToMac(oui, macLong);
                    }else{
                        // if not put success, it already allocated
                        macRangePartition.incUsed();
                        needUpdate = true;
                    }
                }

                if (needUpdate) {
                    macRangePartitionRepository.addItem(macRangePartition);
                }

            } finally {
                try {
                    lock.unlock(id);
                } catch (DistributedLockException e) {
                    logger.error("unlock cluster lock {} failed: {}", id, e.getMessage());
                }
            }
        }
        throw new MacAddressRetryLimitExceedException(MacManagerConstant.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
    }

    @Override
    public Set<String> allocateBulk(String oui, MacRange macRange, int size) throws MacAddressFullException,
            MacAddressRetryLimitExceedException, CacheException{
        Set<String> newAllocatedMacs = new HashSet<>();
        String rangeId = macRange.getRangeId();
        long used = macRangeMappingRepository.size(rangeId);
        if(used + size >= macRange.getCapacity()){
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        }

        int partitionNums = (int) (macRange.getCapacity()/PARTITION_SIZE + 1);
        int partitionIndex = 0;
        int retryTime = 0;
        while (retryTime < nRetryLimit) {

            boolean locked = false;
            while (!locked) {
                partitionIndex = RandomUtils.nextInt(0, partitionNums);
                locked = lock.tryLock(rangeId + "_" + partitionIndex);
            }

            String id = rangeId + "_" + partitionIndex;
            try {
                boolean needUpdate = false;
                MacRangePartition macRangePartition = macRangePartitionRepository.findItem(id);

                // if null create a new one
                if (macRangePartition == null) {
                    macRangePartition = new MacRangePartition(id, partitionIndex, PARTITION_SIZE * partitionIndex,
                            PARTITION_SIZE * (partitionIndex + 1));
                    needUpdate = true;
                }

                while (macRangePartition.getUsed() < macRangePartition.getTotal()) {
                    long macLong = generate(macRangePartition);
                    if(check(rangeId, macLong)) {
                        macRangePartition.incUsed();
                        needUpdate = true;
                        newAllocatedMacs.add(longToMac(oui, macLong));
                        if(newAllocatedMacs.size() >= size){
                            macRangePartitionRepository.addItem(macRangePartition);
                            return newAllocatedMacs;
                        }
                    }else{
                        // if not put success, it already allocated
                        macRangePartition.incUsed();
                        needUpdate = true;
                    }
                }

                if (needUpdate) {
                    macRangePartitionRepository.addItem(macRangePartition);
                }

            } finally {
                try {
                    lock.unlock(id);
                } catch (DistributedLockException e) {
                    logger.error("unlock cluster lock {} failed: {}", id, e.getMessage());
                }
            }
        }
        throw new MacAddressRetryLimitExceedException(MacManagerConstant.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
    }

    @Override
    public Boolean reclaim(String rangeId, String oui, String mac) {
        try {
            Long macLong = macToLong(getMacSuffix(oui, mac));
            return macRangeMappingRepository.releaseMac(rangeId, macLong);
        } catch (CacheException e) {
            return false;
        }
    }

    @Override
    public long rangeSize(String rangeId) throws CacheException {
        return macRangeMappingRepository.size(rangeId);
    }

    @Override
    public long rangeAvailableSize(MacRange macRange) throws CacheException {
        long size = macRangeMappingRepository.size(macRange.getRangeId());
        return macRange.getCapacity() - size;
    }

    @Override
    public void markMac(String rangeId, String oui, String mac) throws CacheException, DistributedLockException {

        long macLong = macToLong(getMacSuffix(oui, mac));
        macRangeMappingRepository.putIfAbsent(rangeId, macLong);

        // mark mac range partition
        int partitionIndex = (int)(macLong/PARTITION_SIZE);
        String id = rangeId + "_" + partitionIndex;

        lock.lock(id);
        try {
            MacRangePartition macRangePartition = macRangePartitionRepository.findItem(id);
            if (macRangePartition == null) {
                macRangePartition = new MacRangePartition(id, partitionIndex, PARTITION_SIZE * partitionIndex,
                        PARTITION_SIZE * (partitionIndex + 1));
            }
            int freeBit = (int)(macLong%PARTITION_SIZE);
            macRangePartition.getBitSet().set(freeBit);
            macRangePartitionRepository.addItem(macRangePartition);
        } finally {
            lock.unlock(id);
        }
    }

    private Long generate(MacRangePartition macRangePartition) throws MacAddressFullException {
        BitSet bitset = macRangePartition.getBitSet();
        int freeBit = bitset.nextClearBit(0);
        long key = freeBit + macRangePartition.getStart();
        if (key < macRangePartition.getStart() || key > macRangePartition.getEnd()){
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        }
        bitset.set(freeBit);
        return macRangePartition.getStart() + freeBit;
    }

    private boolean check(String rangeId, Long macLong){
        try {
            return macRangeMappingRepository.putIfAbsent(rangeId, macLong);
        } catch (CacheException e) {
            return false;
        }
    }
}
