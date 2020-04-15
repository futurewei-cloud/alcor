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

import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
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
