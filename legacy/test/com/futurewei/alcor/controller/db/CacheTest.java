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
     * Since all test cases for this class need to connect to the database,
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