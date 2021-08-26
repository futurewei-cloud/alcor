/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.db.Transaction;
import org.apache.ignite.Ignite;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class IgniteCacheFactory implements ICacheFactory {

    private final Ignite ignite;
    private final int tryLockInterval;
    private final int expireTime;

    public IgniteCacheFactory(Ignite ignite, int interval, int expire) {
        this.ignite = ignite;
        this.tryLockInterval = interval;
        this.expireTime = expire;
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v) {
        return new IgniteDbCache<>(ignite, v.getName());
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        return new IgniteDbCache<>(ignite, cacheName);
    }

    @Override
    public <K, V> ICache<K, V>
    getCache(Class<V> v, CacheConfiguration cacheConfig) { return new IgniteDbCache<>(ignite, cacheConfig); }

    @Override
    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit) {
        ExpiryPolicy ep = CreatedExpiryPolicy.factoryOf(new Duration(timeUnit, timeout)).create();
        return new IgniteDbCache<K, V>(ignite, v.getName(), ep);
    }

    @Override
    public <V> IDistributedLock getDistributedLock(Class<V> t) {
        return new IgniteDistributedLock(ignite, LOCK_PREFIX + t.getName(), this.tryLockInterval, this.expireTime);
    }

    @Override
    public Transaction getTransaction() {
        return new IgniteTransaction(ignite);
    }

}