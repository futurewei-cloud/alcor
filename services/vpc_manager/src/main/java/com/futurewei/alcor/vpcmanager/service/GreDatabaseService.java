package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkGREType;

import java.util.Map;

public interface GreDatabaseService {

    public NetworkGREType getByGreId (String greId);
    public Map getAllGres () throws CacheException;
    public void addGre(NetworkGREType gre) throws DatabasePersistenceException;
    public void deleteGre (String id) throws CacheException;
    public ICache<String, NetworkGREType> getCache ();

}
