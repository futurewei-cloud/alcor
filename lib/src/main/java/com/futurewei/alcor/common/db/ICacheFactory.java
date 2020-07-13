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

import java.util.concurrent.TimeUnit;

public interface ICacheFactory {

    /**
     * get a persistence cache
     * @return
     */
    <K, V> ICache<K, V> getCache(Class<V> v);

    /**
     * get a persistence cache with cache name
     * @return
     */
    <K, V> ICache<K, V> getCache(Class<V> v, String cacheName);

    /**
     * get a cache with auto set expire time
     * @return
     */
    <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit);

    /**
     * get a spin lock
     * @param t class type
     * @return IDistributedLock
     */
    <T> IDistributedLock getDistributedLock(Class<T> t);
}
