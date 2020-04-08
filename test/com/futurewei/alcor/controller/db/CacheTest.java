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

package com.futurewei.alcor.controller.db;

import com.futurewei.alcor.controller.app.AlcorControllerApp;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.model.VpcState;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AlcorControllerApp.class})
public class CacheTest {
    @Autowired
    private CacheFactory cacheFactory;

    private void basicTest() throws CacheException {
        ICache<String, VpcState> cache = cacheFactory.getCache(VpcState.class);
        Assert.assertNotNull(cache);

        //put operation test
        VpcState vpcState1 = new VpcState("project1", "id1", "vpc1", "10.10.10.0/24");
        VpcState vpcState2 = new VpcState("project1", "id2", "vpc2", "11.11.11.0/24");

        cache.put("vpc1", vpcState1);
        cache.put("vpc2", vpcState2);

        //get operation test
        VpcState vpc1 = cache.get("vpc1");
        VpcState vpc2 = cache.get("vpc2");

        Assert.assertEquals(vpcState1, vpc1);
        Assert.assertEquals(vpcState2, vpc2);

        //containsKey operation test
        Assert.assertEquals(true, cache.containsKey("vpc1"));
        Assert.assertEquals(true, cache.containsKey("vpc2"));
        Assert.assertEquals(false, cache.containsKey("vpc3"));

        //getAll operation test
        Map<String, VpcState> vpcs = cache.getAll();
        for (Map.Entry<String, VpcState> entry: vpcs.entrySet()) {
            if ("vpc1".equals(entry.getKey())) {
                Assert.assertEquals(vpcState1, entry.getValue());
            } else {
                Assert.assertEquals(vpcState2, entry.getValue());
            }
        }

        //remove operation test
        cache.remove("vpc1");
        cache.remove("vpc2");

        vpc1 = cache.get("vpc1");
        vpc2 = cache.get("vpc2");

        Assert.assertEquals(null, vpc1);
        Assert.assertEquals(null, vpc2);

        //putAll operation test
        Map vpcMap = new HashMap();

        vpcMap.put("vpc1", vpcState1);
        vpcMap.put("vpc2", vpcState2);

        cache.putAll(vpcMap);

        vpc1 = cache.get("vpc1");
        vpc2 = cache.get("vpc2");

        Assert.assertEquals(vpcState1, vpc1);
        Assert.assertEquals(vpcState2, vpc2);
    }

    private void transactionTest() throws CacheException {
        ICache<String, VpcState> cache = cacheFactory.getCache(VpcState.class);
        Transaction transaction = cache.getTransaction();

        VpcState vpcState1 = new VpcState("project1", "id1", "vpc1", "10.10.10.0/24");
        VpcState vpcState2 = new VpcState("project1", "id2", "vpc2", "11.11.11.0/24");

        transaction.start();

        cache.put("vpc1", vpcState1);
        cache.put("vpc2", vpcState2);

        transaction.commit();

        VpcState vpc1 = cache.get("vpc1");
        VpcState vpc2 = cache.get("vpc2");

        Assert.assertEquals(vpcState1, vpc1);
        Assert.assertEquals(vpcState2, vpc2);
    }

    /**
     * Since all test cases for this class need to be connected to the database,
     * they cannot be executed at compile time, so add a switch canRun to this method,
     * which needs to be turned on when executing these test cases in the development environment.
     * @throws CacheException Exception of cache operation
     */
    @Test
    public void cacheTest() throws CacheException {
        boolean canRun = false;
        if (canRun) {
            basicTest();
            transactionTest();
        }
    }
}