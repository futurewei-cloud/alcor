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
