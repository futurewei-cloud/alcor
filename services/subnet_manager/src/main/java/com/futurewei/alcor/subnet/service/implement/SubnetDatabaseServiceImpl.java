package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.subnet.dao.SubnetRepository;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubnetDatabaseServiceImpl implements SubnetDatabaseService {

    @Autowired
    private SubnetRepository subnetRepository;

    @Override
    @DurationStatistics
    public SubnetEntity getBySubnetId(String subnetId) {
        try {
            return this.subnetRepository.findItem(subnetId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetEntity> getAllSubnets() throws CacheException {
        return this.subnetRepository.findAllItems();
    }

    @Override
    @DurationStatistics
    public Map<String, SubnetEntity> getAllSubnets(Map<String, Object[]> queryParams) throws CacheException {
        return this.subnetRepository.findAllItems(queryParams);
    }

    @Override
    @DurationStatistics
    public void addSubnet(SubnetEntity subnetEntity) throws DatabasePersistenceException {
        try {
            this.subnetRepository.addItem(subnetEntity);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void deleteSubnet(String id) throws CacheException {
        this.subnetRepository.deleteItem(id);
    }
}
