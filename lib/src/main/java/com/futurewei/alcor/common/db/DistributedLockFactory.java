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

import com.futurewei.alcor.common.db.ignite.IgniteDistributedLock;
import com.futurewei.alcor.common.db.redis.RedisDistributedLock;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@ComponentScan
@Component
public class DistributedLockFactory {
    @Autowired(required = false)
    private IgniteClient igniteClient;

    @Value("${lock.try.interval:10}")
    private int tryLockInterval;

    @Value("${lock.expire.time:120}")
    private int expireTime;

    @Autowired
    LettuceConnectionFactory lettuceConnectionFactory;

    private IDistributedLock getIgniteDistributedLock(String name) {
        return new IgniteDistributedLock(igniteClient, name, tryLockInterval, expireTime);
    }

    public IDistributedLock getRedisDistributedLock(String name) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);

        return new RedisDistributedLock(template, name, tryLockInterval, expireTime);
    }

    public <T> IDistributedLock getDistributedLock(Class<T> t) {
        if (igniteClient != null) {
            return getIgniteDistributedLock(t.getName());
        }

        return getRedisDistributedLock(t.getName());
    }
}
