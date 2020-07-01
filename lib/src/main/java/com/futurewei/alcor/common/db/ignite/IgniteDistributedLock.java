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

public class IgniteDistributedLock implements IDistributedLock {
    private static final Logger logger = LoggerFactory.getLogger();
    private final String name;
    private ClientCache<String, String> cache;
    private IgniteClient igniteClient;
    private int tryInterval;

    public IgniteDistributedLock(IgniteClient igniteClient, String name, int tryInterval, int expireTime) {
        this.igniteClient = igniteClient;
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
        Boolean locked = false;
        String lockKeyWithPrefix = this.name + " lock:" + lockKey;

        try {
            while (!locked) {
                locked = cache.putIfAbsent(lockKeyWithPrefix, "lock");
                Assert.notNull(locked, "Ignite lock should not run within a transaction");
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
    public void unlock(String lockKey) throws DistributedLockException {
        String lockKeyWithPrefix = this.name + "lock:" + lockKey;

        try {
            cache.remove(lockKeyWithPrefix);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ignite unlock error:" + e.getMessage());
            throw new DistributedLockException(e.getMessage());
        }
    }
}
