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
package com.futurewei.alcor.common.db;

import com.futurewei.alcor.common.exception.DistributedLockException;

public interface IDistributedLock {

    void lock(String lockKey) throws DistributedLockException;


    void unlock(String lockKey) throws DistributedLockException;

    /**
     * try lock once, return immediately
     * @param lockKey
     * @return true if locked success else return false
     * @throws DistributedLockException
     */
    Boolean tryLock(String lockKey) throws DistributedLockException;

}
