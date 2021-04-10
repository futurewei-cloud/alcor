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


package com.futurewei.alcor.common.db;

import com.futurewei.alcor.common.db.ignite.IgniteCacheFactory;
import com.futurewei.alcor.common.db.ignite.IgniteClientCacheFactory;
import com.futurewei.alcor.common.db.redis.RedisCacheFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@ComponentScan
@Component
public class CacheFactory {

    @Autowired
    private ICacheFactory iCacheFactory;

    public <K, V> ICache<K, V> getCache(Class<V> v) {
        return iCacheFactory.getCache(v);
    }

    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        return iCacheFactory.getCache(v, cacheName);
    }

    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit){
        return iCacheFactory.getExpireCache(v, timeout, timeUnit);
    }
}
