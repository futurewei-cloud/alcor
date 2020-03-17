package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.db.Database;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.SubnetState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(Database.class)
public class SubnetRepository implements ICacheRepository<SubnetState> {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final String KEY = "SubnetState";

    private ICache<String, SubnetState> cache;

    @Autowired
    public SubnetRepository(Database database) {
        cache = database.getSubnetCache(KEY);
    }

    @PostConstruct
    private void init() {}

    @Override
    public SubnetState findItem(String id) {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() {
        return cache.getAll();
    }

    @Override
    public void addItem(SubnetState newItem) {
        logger.log(Level.INFO, "Subnet Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        cache.remove(id);
    }
}
