package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.VpcState;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(IgniteClient.class)
public class VpcIgniteRepository implements ICacheRepository<VpcState> {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final String KEY = "VpcState";

    private ClientCache<String, VpcState> cache;

    @Autowired
    public VpcIgniteRepository(IgniteClient igniteClient) {
        try {
            cache = igniteClient.getOrCreateCache(KEY);
        }
        catch (ClientException e) {
            logger.log(Level.WARNING, "Create cache for vpc failed:" + e.getMessage());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected failure:" + e.getMessage());
        }

        Assert.notNull(igniteClient, "Create cache for vpc failed");
    }

    @PostConstruct
    private void init() {

    }

    @Override
    public VpcState findItem(String id) {
        return (VpcState) cache.get(id);
    }

    @Override
    public Map findAllItems() {
        return null;
    }

    @Override
    public void addItem(VpcState vpcState) {
        logger.log(Level.INFO, "Add vpc, Vpc Id:" + vpcState.getId());
        cache.put(vpcState.getId(), vpcState);
    }

    @Override
    public void deleteItem(String id) {
        logger.log(Level.INFO, "Delete vpc, Vpc Id:" + id);
        cache.remove(id);
    }
}
