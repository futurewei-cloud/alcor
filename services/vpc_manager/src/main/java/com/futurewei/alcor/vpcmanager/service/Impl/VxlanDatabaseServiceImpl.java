package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.vpcmanager.dao.VxlanRepository;
import com.futurewei.alcor.vpcmanager.service.VxlanDatabaseService;
import com.futurewei.alcor.web.entity.NetworkVxlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VxlanDatabaseServiceImpl implements VxlanDatabaseService {

    @Autowired
    VxlanRepository vxlanRepository;

    @Override
    public NetworkVxlanType getByVxlanId(String vxlanId) {
        try {
            return this.vxlanRepository.findItem(vxlanId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllVxlans() throws CacheException {
        return this.vxlanRepository.findAllItems();
    }

    @Override
    public void addVxlan(NetworkVxlanType vxlan) throws DatabasePersistenceException {
        try {
            this.vxlanRepository.addItem(vxlan);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteVxlan(String id) throws CacheException {
        this.vxlanRepository.deleteItem(id);
    }

    @Override
    public ICache<String, NetworkVxlanType> getCache() {
        return this.vxlanRepository.getCache();
    }
}
