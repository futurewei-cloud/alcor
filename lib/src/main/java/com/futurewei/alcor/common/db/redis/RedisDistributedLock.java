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
