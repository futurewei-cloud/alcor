package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.subnet.SubnetWebObject;

import java.util.Map;

public interface SubnetDatabaseService {

    public SubnetWebObject getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllSubnets () throws CacheException;
    public void addSubnet (SubnetWebObject subnetWebObject) throws DatabasePersistenceException;
    public void deleteSubnet (String id) throws CacheException;

}
