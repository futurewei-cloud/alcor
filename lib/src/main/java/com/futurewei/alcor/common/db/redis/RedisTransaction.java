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

    public Transaction start() throws CacheException {
        redisTemplate.setEnableTransactionSupport(true);
        try {
            redisTemplate.multi();
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }

        return this;
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

    @Override
    public void close() {
        //Do nothing
    }
}
