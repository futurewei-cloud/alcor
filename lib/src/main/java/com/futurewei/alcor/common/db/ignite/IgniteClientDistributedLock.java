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

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.springframework.util.Assert;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class IgniteClientDistributedLock implements IDistributedLock {
    private static final Logger logger = LoggerFactory.getLogger();
    private final String name;
    private ClientCache<String, String> cache;
    private int tryInterval;

    public IgniteClientDistributedLock(IgniteClient igniteClient, String name, int tryInterval, int expireTime) {
        this.name = name;

        try {
            ClientCacheConfiguration cfg = new ClientCacheConfiguration();
            ExpiryPolicy ep = CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, expireTime)).create();
            cfg.setName(name);
            cfg.setExpiryPolicy(ep);
            cache = igniteClient.getOrCreateCache(cfg);
            this.tryInterval = tryInterval;
        } catch (ClientException e) {
            logger.log(Level.WARNING, "Create distributed lock cache failed:" + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(igniteClient, "Create distributed lock failed");
    }

    @Override
    public void lock(String lockKey) throws DistributedLockException {
        boolean locked = false;
        String lockKeyWithPrefix = getRealKey(lockKey);

        try {
            while (!locked) {
                locked = cache.putIfAbsent(lockKeyWithPrefix, "lock");
                if (!locked) {
                    Thread.sleep(this.tryInterval);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ignite lock error:" + e.getMessage());
            throw new DistributedLockException(e.getMessage());
        }
    }

    @Override
    public Boolean tryLock(String lockKey){
        String lockKeyWithPrefix = getRealKey(lockKey);
        try {
            return cache.putIfAbsent(lockKeyWithPrefix, "lock");
        }catch (Exception e) {
            logger.log(Level.WARNING, "Ignite lock error:" + e.getMessage());
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) throws DistributedLockException {
        String lockKeyWithPrefix = getRealKey(lockKey);

        try {
            cache.remove(lockKeyWithPrefix);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ignite unlock error:" + e.getMessage());
            throw new DistributedLockException(e.getMessage());
        }
    }

    @Override
    public String getLockPrefix() {
        return this.name;
    }
}
