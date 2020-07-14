/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Ignite thin client
 * Should put AlcorLib jar in Ignite lib directory for this client working well
 */
public class IgniteClientCacheFactory implements ICacheFactory {

    private final IgniteClient igniteClient;
    private final int tryLockInterval;
    private final int expireTime;

    public IgniteClientCacheFactory(IgniteClient igniteClient, int interval, int expire) {
        this.igniteClient = igniteClient;
        this.tryLockInterval = interval;
        this.expireTime = expire;
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v) {
        return new IgniteClientDbCache<>(igniteClient, v.getName());
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        return new IgniteClientDbCache<>(igniteClient, cacheName);
    }

    @Override
    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit) {
        ExpiryPolicy ep = CreatedExpiryPolicy.factoryOf(new Duration(timeUnit, timeout)).create();
        return new IgniteClientDbCache<>(igniteClient, v.getName(), ep);
    }

    @Override
    public <T> IDistributedLock getDistributedLock(Class<T> t) {
        return new IgniteClientDistributedLock(this.igniteClient, LOCK_PREFIX + t.getName(), this.tryLockInterval, this.expireTime);
    }
}
