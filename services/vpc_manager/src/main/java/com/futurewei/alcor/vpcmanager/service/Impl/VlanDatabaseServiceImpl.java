package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.dao.VlanRepository;
import com.futurewei.alcor.vpcmanager.service.VlanDatabaseService;
import com.futurewei.alcor.vpcmanager.entity.NetworkVlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VlanDatabaseServiceImpl implements VlanDatabaseService {

    @Autowired
    VlanRepository vlanRepository;

    @Override
    @DurationStatistics
    public NetworkVlanType getByVlanId(String vlanId) {
        try {
            return this.vlanRepository.findItem(vlanId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map getAllVlans() throws CacheException {
        return this.vlanRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public void addVlan(NetworkVlanType vlan) throws DatabasePersistenceException {
        try {
            this.vlanRepository.addItem(vlan);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteVlan(String id) throws CacheException {
        this.vlanRepository.deleteItem(id);
    }

    @Override
    @DurationStatistics
    public ICache<String, NetworkVlanType> getCache() {
        return this.vlanRepository.getCache();
    }
}
