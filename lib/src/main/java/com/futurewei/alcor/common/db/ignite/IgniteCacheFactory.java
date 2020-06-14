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

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import org.apache.ignite.client.IgniteClient;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;

public class IgniteCacheFactory implements ICacheFactory {

    private IgniteClient igniteClient;

    public IgniteCacheFactory(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v) {
        return new IgniteCache<>(igniteClient, v.getName());
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        return new IgniteCache<>(igniteClient, cacheName);
    }

    @Override
    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit) {
        ExpiryPolicy ep = CreatedExpiryPolicy.factoryOf(new Duration(timeUnit, timeout)).create();
        return new IgniteCache<>(igniteClient, v.getName(), ep);
    }
}
