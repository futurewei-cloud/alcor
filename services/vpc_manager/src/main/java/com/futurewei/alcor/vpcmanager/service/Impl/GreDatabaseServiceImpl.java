package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.vpcmanager.dao.GreRepository;
import com.futurewei.alcor.vpcmanager.service.GreDatabaseService;
import com.futurewei.alcor.web.entity.NetworkVGREType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GreDatabaseServiceImpl implements GreDatabaseService {

    @Autowired
    GreRepository greRepository;

    @Override
    public NetworkVGREType getByGreId(String greId) {
        try {
            return this.greRepository.findItem(greId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllGres() throws CacheException {
        return this.greRepository.findAllItems();
    }

    @Override
    public void addGre(NetworkVGREType gre) throws DatabasePersistenceException {
        try {
            this.greRepository.addItem(gre);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteGre(String id) throws CacheException {
        this.greRepository.deleteItem(id);
    }

    @Override
    public ICache<String, NetworkVGREType> getCache() {
        return this.greRepository.getCache();
    }
}
