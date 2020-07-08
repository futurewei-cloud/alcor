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
import com.futurewei.alcor.common.db.IDistributedLockFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisDistributedLockFactory implements IDistributedLockFactory {

    private final LettuceConnectionFactory lettuceConnectionFactory;
    private final int tryLockInterval;
    private final int expireTime;

    public RedisDistributedLockFactory(LettuceConnectionFactory connectionFactory, int interval, int expire) {
        this.lettuceConnectionFactory = connectionFactory;
        this.tryLockInterval = interval;
        this.expireTime = expire;
    }

    public <T> IDistributedLock getDistributedLock(Class<T> t) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);

        return new RedisDistributedLock(template, t.getName(), tryLockInterval, expireTime);
    }

}
