package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.GatewayType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GatewayRepository implements ICacheRepository<GatewayInfo> {

    private final ICache<String, GatewayInfo> cache;
    private final ICache<String, GatewayEntity> gatewayEntityCache;
    private final ICache<String, GWAttachment> attachmentCache;


    @Autowired
    public GatewayRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(GatewayInfo.class);
        this.gatewayEntityCache = cacheFactory.getCache(GatewayEntity.class);
        this.attachmentCache = cacheFactory.getCache(GWAttachment.class);
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
        log.debug("Add GatewayInfo, GatewayInfo : {}", gatewayInfo);
        cache.put(gatewayInfo.getResourceId(), gatewayInfo);
    }

    @Override
    public void addItems(List<GatewayInfo> items) throws CacheException {
        Map<String, GatewayInfo> gatewayInfoMap = items.stream().collect(Collectors.toMap(GatewayInfo::getResourceId, Function.identity()));
        cache.putAll(gatewayInfoMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete GatewayInfo, resource_id is: {}", id);
        cache.remove(id);
    }

    public void deleteGatewayEntity(String gatewayId) throws CacheException {
        log.debug("Delete GatewayEntity, gatewayId is: {}", gatewayId);
        gatewayEntityCache.remove(gatewayId);
    }

    public void deleteGatewayInfoForZeta(String vpcId, GatewayInfo gatewayInfo, Map<String, GWAttachment> attachmentsMap) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            boolean flag;
            List<GatewayEntity> gatewayEntities = gatewayInfo.getGatewayEntities();
            Iterator<GatewayEntity> iterator = gatewayEntities.iterator();
            while (iterator.hasNext()) {
                GatewayEntity gatewayEntity = iterator.next();
                flag = false;
                for (GWAttachment attachment : attachmentsMap.values()) {
                    if (attachment.getResourceId().equals(vpcId) && attachment.getGatewayId().equals(gatewayEntity.getId())
                            && gatewayEntity.getType().equals(GatewayType.ZETA)) {
                        flag = true;
                        attachmentCache.remove(attachment.getId());
                    }
                }
                if (flag) {
                    iterator.remove();
                }
            }
            cache.put(gatewayInfo.getResourceId(), gatewayInfo);
            tx.commit();
        }
    }
}
