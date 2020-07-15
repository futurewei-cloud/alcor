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
import com.futurewei.alcor.macmanager.dao.MacRangeMappingRepository;
import com.futurewei.alcor.macmanager.exception.MacAddressFullException;
import com.futurewei.alcor.macmanager.exception.MacAddressRetryLimitExceedException;
import com.futurewei.alcor.macmanager.pool.MacPoolApi;
import com.futurewei.alcor.macmanager.utils.MacManagerConstant;
import com.futurewei.alcor.web.entity.mac.MacRange;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.futurewei.alcor.macmanager.utils.MacUtils.*;

@Component
public class RandomMacPoolImpl implements MacPoolApi {

    private static final int MUTIL_QUERY_THRESHOLD = 50;

    private static final float BOTTOM_LOAD_FACTOR = 0.1f;
    private static final float LOW_LOAD_FACTOR = 0.25f;
    private static final float MIDDLE_LOAD_FACTOR = 0.5f;
    private static final float HIGH_LOAD_FACTOR = 0.75f;

    @Value("${low-request-numbers: 10}")
    private int lowRequestNumbers;

    @Value("${middle-request-numbers: 20}")
    private int middleRequestNumbers;

    @Value("${high-request-numbers: 60}")
    private int highRequestNumbers;

    @Value("${top-request-numbers: 400}")
    private int topRequestNumbers;

    @Value("${macmanager.retrylimit}")
    private long nRetryLimit;

    @Autowired
    private MacRangeMappingRepository macRangeMappingRepository;

    @Override
    public String allocate(String oui, MacRange macRange) throws MacAddressFullException, MacAddressRetryLimitExceedException, CacheException {

        String rangeId = macRange.getRangeId();
        long used = macRangeMappingRepository.size(rangeId);
        String fromHexSuffix = getMacSuffix(oui, macRange.getFrom());
        String toHexSuffix = getMacSuffix(oui, macRange.getTo());
        long start = macToLong(fromHexSuffix);
        long end = macToLong(toHexSuffix);

        if(used >= macRange.getCapacity()){
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        }

        float loadRate = (float) used/macRange.getCapacity();

        int retryTime = 0;
        do {
            if(retryTime >= nRetryLimit){
                throw new MacAddressRetryLimitExceedException(MacManagerConstant.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
            }
            long next = RandomUtils.nextLong(start, end);
            if (loadRate < BOTTOM_LOAD_FACTOR) {
                if(check(rangeId, next)){
                    return longToMac(oui, next);
                }
            }else {
                int requestNumbers = getRequestNumbers(loadRate);
                requestNumbers = requestNumbers > macRange.getCapacity() ? (int) macRange.getCapacity() : requestNumbers;
                long left = next - requestNumbers / 2;
                long right = next + requestNumbers / 2 + requestNumbers % 2;

                if (left < start) {
                    right += start - left;
                    left = start;
                }

                if (right > end) {
                    left -= right - end;
                    right = end + 1;
                }

                if(requestNumbers > MUTIL_QUERY_THRESHOLD){
                    Set<Long> macs = new HashSet<>();
                    for (long i = left; i < right; i++) {
                        macs.add(i);
                    }
                    Set<Long> newMacs = checkMulti(rangeId, macs);
                    if(newMacs != null){
                        for(Long macLong: newMacs){
                            if(check(rangeId, macLong)){
                                return longToMac(oui, macLong);
                            }
                        }
                    }
                } else {
                    for (long i = left; i < right; i++) {
                        if(check(rangeId, i)){
                            return longToMac(oui, i);
                        };
                    }
                }
            }
            retryTime ++;
        }while(retryTime < nRetryLimit);
        throw new MacAddressRetryLimitExceedException(MacManagerConstant.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
    }

    @Override
    public Set<String> allocateBulk(String oui, MacRange macRange, int size) throws MacAddressFullException, MacAddressRetryLimitExceedException, CacheException{
        Set<String> newAllocateMacs = new HashSet<>(size);
        String rangeId = macRange.getRangeId();
        long used = macRangeMappingRepository.size(rangeId);
        String fromHexSuffix = getMacSuffix(oui, macRange.getFrom());
        String toHexSuffix = getMacSuffix(oui, macRange.getTo());
        long start = macToLong(fromHexSuffix);
        long end = macToLong(toHexSuffix);

        if(used >= macRange.getCapacity()){
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        }

        int retryTime = 0;
        do {
            if(retryTime >= nRetryLimit){
                throw new MacAddressRetryLimitExceedException(MacManagerConstant.MAC_EXCEPTION_RETRY_LIMIT_EXCEED);
            }
            long next = RandomUtils.nextLong(start, end);

            int requestNumbers = size;
            requestNumbers = requestNumbers > macRange.getCapacity() ? (int) macRange.getCapacity() : requestNumbers;
            long left = next - requestNumbers / 2;
            long right = next + requestNumbers / 2 + requestNumbers % 2;

            if (left < start) {
                right += start - left;
                left = start;
            }

            if (right > end) {
                left -= right - end;
                right = end + 1;
            }

            Set<Long> macs = new HashSet<>();
            for (long i = left; i < right; i++) {
                macs.add(next);
            }
            Set<Long> newMacs = checkMulti(rangeId, macs);
            if(newMacs != null){
                for(Long macLong: newMacs){
                    if(check(rangeId, macLong)){
                        newAllocateMacs.add(longToMac(oui, macLong));
                    }
                }
            }
            if (newAllocateMacs.size() >= size){
                return newAllocateMacs;
            }
            retryTime ++;
        }while(retryTime < nRetryLimit);
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
    public void markMac(String rangeId, String oui, String mac) throws CacheException {
        Long macLong = macToLong(getMacSuffix(oui, mac));
        macRangeMappingRepository.putIfAbsent(rangeId, macLong);
    }

    private int getRequestNumbers(float loadRate){
        if (loadRate < LOW_LOAD_FACTOR){
            return lowRequestNumbers;
        }else if (loadRate < MIDDLE_LOAD_FACTOR){
            return middleRequestNumbers;
        }else if (loadRate < HIGH_LOAD_FACTOR){
            return highRequestNumbers;
        }
        return topRequestNumbers;
    }

    private boolean check(String rangeId, Long macLong){
        try {
            return macRangeMappingRepository.putIfAbsent(rangeId, macLong);
        } catch (CacheException e) {
            return false;
        }
    }

    private Set<Long> checkMulti(String rangeId, Set<Long> macs){
        // get too many result change to bulk handle
        try {
            return macRangeMappingRepository.getAll(rangeId, macs);
        } catch (CacheException e) {
            return null;
        }
    }
}
