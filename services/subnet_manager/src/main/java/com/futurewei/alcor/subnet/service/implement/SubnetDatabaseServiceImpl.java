package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.subnet.dao.SubnetRepository;
import com.futurewei.alcor.subnet.entity.SubnetState;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubnetDatabaseServiceImpl implements SubnetDatabaseService {

    @Autowired
    private SubnetRepository subnetRepository;

    @Override
    public SubnetState getBySubnetId(String subnetId) {
        try {
            return this.subnetRepository.findItem(subnetId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllSubnets() throws CacheException {
        return this.subnetRepository.findAllItems();
    }

    @Override
    public void addSubnet(SubnetState subnetState) throws DatabasePersistenceException {
        try {
            this.subnetRepository.addItem(subnetState);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteSubnet(String id) throws CacheException {
        this.subnetRepository.deleteItem(id);
    }
}
