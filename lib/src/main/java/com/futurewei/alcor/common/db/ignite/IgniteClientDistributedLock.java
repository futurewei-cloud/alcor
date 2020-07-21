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
