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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import org.apache.ignite.lang.IgniteBiPredicate;

import java.util.Map;

public interface IgniteICache<K, V> extends ICache<K, V> {

    /**
     * Get Cache value from cache db by a {@link IgniteBiPredicate}
     *
     * @param igniteBiPredicate a implement of {@link IgniteBiPredicate}
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> V get(IgniteBiPredicate<E1, E2> igniteBiPredicate) throws CacheException;

    /**
     * Get Cache values from cache db by a {@link IgniteBiPredicate}
     *
     * @param igniteBiPredicate a implement of {@link IgniteBiPredicate}
     * @return cache value
     * @throws CacheException if any exception
     */
    <E1, E2> Map<K, V> getAll(IgniteBiPredicate<E1, E2> igniteBiPredicate) throws CacheException;

}
