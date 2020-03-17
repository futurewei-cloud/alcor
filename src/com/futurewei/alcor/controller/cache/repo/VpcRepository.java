package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.db.Database;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.VpcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(Database.class)
public class VpcRepository implements ICacheRepository<VpcState> {
    private static final Logger logger = LoggerFactory.getLogger();
    private static final String KEY = "VpcState";

    private ICache<String, VpcState> cache;

    @Autowired
    public VpcRepository(Database database) {
        cache = database.getVpcCache(KEY);
    }

    @PostConstruct
    private void init() {

    }

    @Override
    public VpcState findItem(String id) {
        return cache.get(id);
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
