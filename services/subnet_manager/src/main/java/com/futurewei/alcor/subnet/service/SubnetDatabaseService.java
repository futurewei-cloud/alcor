package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;

import java.util.Map;

public interface SubnetDatabaseService {

    public SubnetEntity getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;

    public Map<String, SubnetEntity> getAllSubnets () throws CacheException;

    public Map<String, SubnetEntity> getAllSubnets (Map<String, Object[]> queryParams) throws CacheException;

    public void addSubnet (SubnetEntity subnetEntity) throws DatabasePersistenceException;

    public void deleteSubnet (String id) throws CacheException;

}
