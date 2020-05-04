package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.web.entity.NetworkVGREType;

import java.util.Map;

public interface GreDatabaseService {

    public NetworkVGREType getByGreId (String greId);
    public Map getAllGres () throws CacheException;
    public void addGre(NetworkVGREType gre) throws DatabasePersistenceException;
    public void deleteGre (String id) throws CacheException;
    public ICache<String, NetworkVGREType> getCache ();

}
