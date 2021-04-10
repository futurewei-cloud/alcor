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


package com.futurewei.alcor.common.service;

import com.futurewei.alcor.common.repo.ICache;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@ComponentScan
@Component
public class CacheFactory {
    @Autowired(required = false)
    private IgniteClient igniteClient;

    @Autowired
    LettuceConnectionFactory lettuceConnectionFactory;

    @Bean
    CacheFactory cacheFactoryInstance() {
        return new CacheFactory();
    }

    private ICache getIgniteCache(String cacheName) {
        return new IgniteCache<>(igniteClient, cacheName);
    }

    public <K, V> ICache getRedisCache(Class<V> v, String cacheName) {

        RedisTemplate<K, V> template = new RedisTemplate<K, V>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.afterPropertiesSet();

        return new RedisCache<>(template, cacheName);
    }

    public <K, V> ICache getCache(Class<V> v) {
        if (igniteClient != null) {
            return getIgniteCache(v.getName());
        }

        return getRedisCache(v, v.getName());
    }
}
