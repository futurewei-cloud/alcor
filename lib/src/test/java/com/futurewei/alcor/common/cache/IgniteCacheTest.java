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
