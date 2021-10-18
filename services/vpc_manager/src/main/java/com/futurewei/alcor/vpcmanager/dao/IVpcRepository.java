package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.repo.ICacheRepository;

import java.util.Set;

public interface IVpcRepository<T> extends ICacheRepository<T> {
    public Set<String> getSubnetIds(String vpcId) throws CacheException;

    public void addSubnetId(String vpcId, String subnetId) throws CacheException;

    public void deleteSubnetId(String vpcId, String subnetId) throws CacheException;
}
