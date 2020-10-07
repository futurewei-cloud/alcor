package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.Map;

public interface VpcDatabaseService {

    public VpcEntity getByVpcId (String vpcId) throws ResourceNotFoundException, ResourcePersistenceException;
    public Map getAllVpcs () throws CacheException;
    public Map getAllVpcs (Map<String, Object[]> queryParams) throws CacheException;
    public void addVpc (VpcEntity vpcState) throws DatabasePersistenceException;
    public void deleteVpc (String id) throws CacheException;
    public ICache<String, VpcEntity> getCache ();

}
