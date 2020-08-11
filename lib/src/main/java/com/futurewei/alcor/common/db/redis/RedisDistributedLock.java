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

package com.futurewei.alcor.common.db.redis;

import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RedisDistributedLock implements IDistributedLock {
    private static final Logger logger = LoggerFactory.getLogger();

    private final StringRedisTemplate redisTemplate;
    private final String name;
    private final int tryInterval;
    private final int expireTime;

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String name, int tryInterval, int expireTime) {
        this.redisTemplate = redisTemplate;
        this.name = name;
        this.tryInterval = tryInterval;
        this.expireTime = expireTime;
    }

    @Override
    public void lock(String lockKey) throws DistributedLockException {
        Boolean locked = false;
        String lockKeyWithPrefix = getRealKey(lockKey);

        try {
            while (!locked) {
                locked = redisTemplate.opsForValue().setIfAbsent(lockKeyWithPrefix, "lock",
                        this.expireTime, TimeUnit.SECONDS);
                Assert.notNull(locked, "Redis lock should not run within a transaction");
                if (!locked) {
                    Thread.sleep(this.tryInterval);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Redis lock error:" + e.getMessage());
            throw new DistributedLockException(e.getMessage());
        }
    }

    @Override
    public void unlock(String lockKey) throws DistributedLockException {
        String lockKeyWithPrefix = getRealKey(lockKey);

        try {
            redisTemplate.delete(lockKeyWithPrefix);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Redis unlock error:" + e.getMessage());
            throw new DistributedLockException(e.getMessage());
        }
    }

    @Override
    public Boolean tryLock(String lockKey){
        String lockKeyWithPrefix = getRealKey(lockKey);
        try {
            return redisTemplate.opsForValue().setIfAbsent(lockKeyWithPrefix, "lock",
                    this.expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Redis lock error:" + e.getMessage());
            return false;
        }
    }

    @Override
    public String getLockPrefix() {
        return this.name;
    }
}
