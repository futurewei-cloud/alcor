package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.SubnetState;

import java.util.Map;

public interface SubnetDatabaseService {

    public SubnetState getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllSubnets () throws CacheException;
    public void addSubnet (SubnetState subnetState) throws DatabasePersistenceException;
    public void deleteSubnet (String id) throws CacheException;

}
