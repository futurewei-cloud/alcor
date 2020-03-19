package com.futurewei.alcor.controller.db.redis;

import com.futurewei.alcor.controller.db.Transaction;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.logging.Level;


public class RedisTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger();

    private RedisTemplate redisTemplate;

    public RedisTransaction(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void start() throws CacheException {
        redisTemplate.setEnableTransactionSupport(true);
        try {
            redisTemplate.multi();
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void commit() throws CacheException {
        try {
            redisTemplate.exec();
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisTransaction commit error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void rollback() throws CacheException {
        try {
            redisTemplate.discard();
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisTransaction rollback error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void close() {
        //Do nothing
    }
}
