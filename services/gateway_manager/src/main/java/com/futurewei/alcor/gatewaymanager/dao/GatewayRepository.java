package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GatewayEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GatewayRepository implements ICacheRepository<GatewayEntity> {
    @Override
    public GatewayEntity findItem(String id) throws CacheException {
        return null;
    }

    @Override
    public Map<String, GatewayEntity> findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map<String, GatewayEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return null;
    }

    @Override
    public void addItem(GatewayEntity newItem) throws CacheException {

    }

    @Override
    public void addItems(List<GatewayEntity> items) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {

    }
}
