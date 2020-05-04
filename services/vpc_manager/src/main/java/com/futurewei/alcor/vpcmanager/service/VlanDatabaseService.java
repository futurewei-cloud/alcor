package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkVlanType;

import java.util.Map;

public interface VlanDatabaseService {

    public NetworkVlanType getByVlanId (String vlanId);
    public Map getAllVlans () throws CacheException;
    public void addVlan (NetworkVlanType vlan) throws DatabasePersistenceException;
    public void deleteVlan (String id) throws CacheException;
    public ICache<String, NetworkVlanType> getCache ();

}
