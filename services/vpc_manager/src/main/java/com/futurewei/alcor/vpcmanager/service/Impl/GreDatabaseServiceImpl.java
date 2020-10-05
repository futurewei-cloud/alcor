package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.GreRepository;
import com.futurewei.alcor.vpcmanager.service.GreDatabaseService;
import com.futurewei.alcor.vpcmanager.entity.NetworkGREType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GreDatabaseServiceImpl implements GreDatabaseService {

    @Autowired
    GreRepository greRepository;

    @Override
    @DurationStatistics
    public NetworkGREType getByGreId(String greId) {
        try {
            return this.greRepository.findItem(greId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllGres() throws CacheException {
        return this.greRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addGre(NetworkGREType gre) throws DatabasePersistenceException {
        try {
            this.greRepository.addItem(gre);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteGre(String id) throws CacheException {
        this.greRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, NetworkGREType> getCache() {
        return this.greRepository.getCache();
    }
}
