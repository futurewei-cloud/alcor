/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
