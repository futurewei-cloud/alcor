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

import com.futurewei.alcor.common.db.ignite.IgniteCacheFactory;
import com.futurewei.alcor.common.db.redis.RedisCacheFactory;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@ComponentScan
@Component
public class CacheFactory {

    private ICacheFactory iCacheFactory;

    public CacheFactory(@Autowired(required=false)IgniteClient igniteClient, LettuceConnectionFactory lettuceConnectionFactory){
        if(igniteClient == null){
            this.iCacheFactory = new RedisCacheFactory(lettuceConnectionFactory);
        }else{
            this.iCacheFactory = new IgniteCacheFactory(igniteClient);
        }
    }

    public <K, V> ICache<K, V> getCache(Class<V> v) {
        return iCacheFactory.getCache(v);
    }

    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit){
        return iCacheFactory.getExpireCache(v, timeout, timeUnit);
    }
}
