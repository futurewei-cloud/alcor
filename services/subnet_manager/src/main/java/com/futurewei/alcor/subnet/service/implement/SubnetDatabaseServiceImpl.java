package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.subnet.dao.SubnetRedisRepository;
import com.futurewei.alcor.subnet.entity.SubnetState;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubnetDatabaseServiceImpl implements SubnetDatabaseService {

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    @Override
    public SubnetState getBySubnetId(String subnetId) {
        try {
            return this.subnetRedisRepository.findItem(subnetId);
        }catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map getAllSubnets() {
        return this.subnetRedisRepository.findAllItems();
    }

    @Override
    public void addSubnet(SubnetState subnetState) {
        this.subnetRedisRepository.addItem(subnetState);
    }

    @Override
    public void deleteSubnet(String id) {
        this.subnetRedisRepository.deleteItem(id);
    }
}
