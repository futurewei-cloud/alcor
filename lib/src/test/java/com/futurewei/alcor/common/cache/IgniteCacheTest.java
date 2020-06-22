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

package com.futurewei.alcor.common.cache;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ignite.IgniteDbCache;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IgniteCacheTest extends MockIgniteServer{

    private static final String CACHE_NAME = CustomerResource.class.getName();

    @Test
    public void multiParamsTest() throws CacheException {
        ICache<String, CustomerResource> cache = new IgniteDbCache<>(MockIgniteServer.getIgnite(), CACHE_NAME);
        Map<String, CustomerResource> map = new HashMap<>();
        map.put("customerA", new CustomerResource("1", "1", "resourceA", "desc1"));
        map.put("customerB", new CustomerResource("2", "2", "resourceB", "desc1"));
        map.put("customerC", new CustomerResource("3", "3", "resourceC", "desc1"));
        map.put("customerD", new CustomerResource("4", "4", "resourceD", "desc2"));
        map.put("customerE", new CustomerResource("5", "5", "resourceE", "desc2"));
        map.put("customerF", new CustomerResource("6", "6", "resourceF", "desc2"));
        map.put("customerG", new CustomerResource("7", "7", "resourceG", "desc2"));
        map.put("customerH", new CustomerResource("8", "8", "resourceH", "desc3"));
        map.put("customerI", new CustomerResource("9", "9", "resourceI", "desc3"));
        cache.putAll(map);
        Map<String, Object[]> params =
                new ImmutableMap.Builder<String, Object[]>().put("projectId", new String[]{"1"}).put("name", new String[]{"resourceA"}).build();
        CustomerResource cr = cache.get(params);
        System.out.println(cr);
        Assert.assertEquals(new CustomerResource("1", "1", "resourceA", "desc1"), cr);

        Map<String, Object[]> params1 =
                new ImmutableMap.Builder<String, Object[]>().put("projectId", new String[]{"1"}).put("name", new String[]{"resourceB"}).build();
        CustomerResource cr1 = cache.get(params1);
        System.out.println(cr1);
        Assert.assertNull(cr1);

        Map<String, Object[]> params2 =
                new ImmutableMap.Builder<String, Object[]>().put("description", new String[]{"desc1", "desc2"}).build();
        Map<String, CustomerResource> crs1 = cache.getAll(params2);
        System.out.println(crs1.toString());
        Assert.assertEquals(7, crs1.size());

    }

    @After
    public void clear(){
        MockIgniteServer.getIgnite().close();
    }
}
