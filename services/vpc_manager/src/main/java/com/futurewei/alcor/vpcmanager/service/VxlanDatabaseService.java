package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkVxlanType;

import java.util.Map;

public interface VxlanDatabaseService {

    public NetworkVxlanType getByVxlanId (String vxlanId);
    public Map getAllVxlans () throws CacheException;
    public void addVxlan (NetworkVxlanType vxlan) throws DatabasePersistenceException;
    public void deleteVxlan (String id) throws CacheException;
    public ICache<String, NetworkVxlanType> getCache ();

}
