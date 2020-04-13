package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.SubnetState;

import java.util.Map;

public interface SubnetRedisService {

    public SubnetState getBySubnetId (String subnetId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllSubnets ();
    public void addSubnet (SubnetState subnetState);
    public void deleteSubnet (String id);

}
