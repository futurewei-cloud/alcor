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


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AlcorControllerApp.class})
public class MultiClientTest1 {
    @Autowired
    private CacheFactory cacheFactory;

    public void multiClientTest1() throws CacheException, InterruptedException {
        ICache<String, VpcState> vpcCache = cacheFactory.getCache(VpcState.class);
        ICache<String, Integer> clientNumCache = cacheFactory.getCache(Integer.class);

        Assert.assertNotNull(vpcCache);
        Assert.assertNotNull(clientNumCache);

        System.out.println("Client1 igniteClient:" + cacheFactory.getIgniteClient());

        VpcState vpcState1 = new VpcState("project1", "id1", "vpc1", "10.10.10.0/24");
        VpcState vpcState2 = new VpcState("project1", "id1", "vpc1", "11.11.11.0/24");

        vpcCache.put("vpc1", vpcState1);
        VpcState vpc1 = vpcCache.get("vpc1");

        Assert.assertEquals(vpcState1, vpc1);

        clientNumCache.put("client_num", 1);
        Integer clientNum = clientNumCache.get("client_num");

        while (clientNum < 2) {
            System.out.println("Waiting for more clients to write the same key");
            Thread.sleep(1000);
            clientNum = clientNumCache.get("client_num");
        }

        vpc1 = vpcCache.get("vpc1");

        Assert.assertEquals(vpcState2, vpc1);
    }

    /**
     * Since all test cases for this class need to be connected to the database,
     * they cannot be executed at compile time, so add a switch canRun to this method,
     * which needs to be turned on when executing these test cases in the development environment.
     * Execute MultiClientTest1.multiClientTest(), and then execute MultiClientTest2.multiClientTest()
     */
    @Test
    public void multiClientTest() throws CacheException, InterruptedException {
        boolean canRun = false;
        if (canRun) {
            multiClientTest1();
        }
    }
}
