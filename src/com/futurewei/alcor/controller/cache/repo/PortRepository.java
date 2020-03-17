package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.db.Database;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.PortState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(Database.class)
public class PortRepository implements ICacheRepository<PortState> {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final String KEY = "PortState";

    private ICache<String, PortState> cache;

    @Autowired
    public PortRepository(Database database) {
        cache = database.getPortCache(KEY);
    }

    @PostConstruct
    private void init() {}

    @Override
    public PortState findItem(String id) {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() {
        return cache.getAll();
    }

    @Override
    public void addItem(PortState newItem) {
        logger.log(Level.INFO, "Port Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        cache.remove(id);
    }
}
