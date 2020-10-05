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

package com.futurewei.alcor.macmanager.pool;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.macmanager.exception.MacAddressFullException;
import com.futurewei.alcor.macmanager.exception.MacAddressRetryLimitExceedException;
import com.futurewei.alcor.web.entity.mac.MacRange;

import java.util.Set;

public interface MacPoolApi {

    /**
     * allocate a new mac
     * @return
     */
    String allocate(String oui, MacRange macRange) throws MacAddressFullException, MacAddressRetryLimitExceedException, CacheException;

    /**
     * allocate multi macs once
     * @param size
     * @return
     */
    Set<String> allocateBulk(String oui, MacRange macRange, int size)  throws MacAddressFullException, MacAddressRetryLimitExceedException, CacheException ;

    /**
     * reclaim a mac
     * @param rangeId
     * @param mac
     * @return
     */
    Boolean release(String rangeId, String oui, String mac);

    /**
     * allocate a new mac form foreign
     * @param rangeId
     * @param oui
     * @param mac
     */
    void markMac(String rangeId, String oui, String mac) throws CacheException, DistributedLockException;

    /**
     * get a range total size
     * @param rangeId
     * @return
     */
    long getRangeSize(String rangeId) throws CacheException;

    /**
     * get a range available size
     * @param macRange
     * @return
     */
    long getRangeAvailableSize(MacRange macRange) throws CacheException ;
}
