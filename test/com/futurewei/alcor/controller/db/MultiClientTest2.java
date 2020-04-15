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
public class MultiClientTest2 {
    @Autowired
    private CacheFactory cacheFactory;

    @Test
    public void multiClientTest2() throws CacheException {
        ICache<String, VpcState> vpcCache = cacheFactory.getCache(VpcState.class);
        ICache<String, Integer> clientNumCache = cacheFactory.getCache(Integer.class);

        Assert.assertNotNull(vpcCache);
        Assert.assertNotNull(clientNumCache);

        System.out.println("Client2 igniteClient:" + cacheFactory.getIgniteClient());

        VpcState vpcState1 = new VpcState("project1", "id1", "vpc1", "11.11.11.0/24");

        vpcCache.put("vpc1", vpcState1);
        VpcState vpc1 = vpcCache.get("vpc1");

        Assert.assertEquals(vpcState1, vpc1);

        Transaction tx = clientNumCache.getTransaction();
        tx.start();

        Integer clientNum = clientNumCache.get("client_num");
        clientNum++;
        clientNumCache.put("client_num", clientNum);

        tx.commit();
    }

    /**
     * Since all test cases for this class need to connect to the database,
     * they cannot be executed at compile time, so add a switch canRun to this method,
     * which needs to be turned on when executing these test cases in the development environment.
     * MultiClientTest1.multiClientTest() needs to be executed before this test case can be executed.
     */
    @Test
    public void multiClientTest() throws CacheException {
        boolean canRun = false;
        if (canRun) {
            multiClientTest2();
        }
    }
}