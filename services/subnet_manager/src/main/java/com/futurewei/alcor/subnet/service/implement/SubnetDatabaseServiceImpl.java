package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.subnet.dao.SubnetRepository;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.web.entity.SubnetWebObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubnetDatabaseServiceImpl implements SubnetDatabaseService {

    @Autowired
    private SubnetRepository subnetRepository;

    @Override
    public SubnetWebObject getBySubnetId(String subnetId) {
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
    public void addSubnet(SubnetWebObject subnetWebObject) throws DatabasePersistenceException {
        try {
            this.subnetRepository.addItem(subnetWebObject);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void deleteSubnet(String id) throws CacheException {
        this.subnetRepository.deleteItem(id);
    }
}
