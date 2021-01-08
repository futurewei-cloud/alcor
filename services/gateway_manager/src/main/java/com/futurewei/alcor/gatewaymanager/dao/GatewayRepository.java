package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GatewayEntity;
import com.futurewei.alcor.gatewaymanager.entity.GatewayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GatewayRepository implements ICacheRepository<GatewayInfo> {

    private final ICache<String,GatewayInfo> cache;
    private final ICache<String, GatewayEntity> entityCache;

    @Autowired
    public GatewayRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(GatewayInfo.class);
        this.entityCache = cacheFactory.getCache(GatewayEntity.class);
    }

    @Override
    public GatewayInfo findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, GatewayInfo> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, GatewayInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(GatewayInfo gatewayInfo) throws CacheException {
        log.debug("Add GatewayInfo, GatewayInfo : {}",gatewayInfo);
        cache.put(gatewayInfo.getResourceId(),gatewayInfo);
    }

    @Override
    public void addItems(List<GatewayInfo> items) throws CacheException {
        Map<String, GatewayInfo> gatewayInfoMap = items.stream().collect(Collectors.toMap(GatewayInfo::getResourceId, Function.identity()));
        cache.putAll(gatewayInfoMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete GatewayInfo, resource_id is: {}",id);
        cache.remove(id);
    }

    public void deleteGatewayEntity(String gatewayId) throws CacheException {
        log.debug("Delete GatewayEntity, gatewayId is: {}",gatewayId);
        entityCache.remove(gatewayId);
    }
}
