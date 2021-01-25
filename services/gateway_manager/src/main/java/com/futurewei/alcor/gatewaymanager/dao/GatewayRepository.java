package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GatewayRepository implements ICacheRepository<GatewayEntity> {

    private final ICache<String, GatewayEntity> gatewayEntityCache;
    private final ICache<String, GWAttachment> attachmentCache;


    @Autowired
    public GatewayRepository(CacheFactory cacheFactory) {
        this.gatewayEntityCache = cacheFactory.getCache(GatewayEntity.class);
        this.attachmentCache = cacheFactory.getCache(GWAttachment.class);
    }

    @Override
    public GatewayEntity findItem(String id) throws CacheException {
        return gatewayEntityCache.get(id);
    }

    @Override
    public Map<String, GatewayEntity> findAllItems() throws CacheException {
        return gatewayEntityCache.getAll();
    }

    @Override
    public Map<String, GatewayEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return gatewayEntityCache.getAll(queryParams);
    }

    @Override
    public void addItem(GatewayEntity gatewayEntity) throws CacheException {
        log.debug("Add GatewayEntity, gatewayEntity : {}", gatewayEntity);
        gatewayEntityCache.put(gatewayEntity.getId(), gatewayEntity);
    }

    @Override
    public void addItems(List<GatewayEntity> items) throws CacheException {
        Map<String, GatewayEntity> gatewayEntityMap = items.stream().collect(Collectors.toMap(GatewayEntity::getId, Function.identity()));
        gatewayEntityCache.putAll(gatewayEntityMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("Delete GatewayEntity, GatewayEntity id is: {}", id);
        gatewayEntityCache.remove(id);
    }

    public void deleteGatewayInfoForZeta(Map<String, GWAttachment> attachmentsMap) throws Exception {
        try (Transaction tx = gatewayEntityCache.getTransaction().start()) {
            for (GWAttachment attachment : attachmentsMap.values()) {
                GatewayEntity gatewayEntity = gatewayEntityCache.get(attachment.getGatewayId());
                if (gatewayEntity == null) {
                    attachmentCache.remove(attachment.getId());
                    continue;
                }
                if (GatewayType.ZETA.equals(gatewayEntity.getType())) {
                    attachmentCache.remove(attachment.getId());
                    gatewayEntityCache.remove(gatewayEntity.getId());
                }
            }
            tx.commit();
        }
    }

    public void addGatewayAndAttachment(GatewayEntity gatewayEntity, GWAttachment attachment) throws Exception {
        try (Transaction tx = gatewayEntityCache.getTransaction().start()) {
            log.info("Add GatewayEntity, gatewayEntity's id is: {}", gatewayEntity.getId());
            gatewayEntityCache.put(gatewayEntity.getId(), gatewayEntity);
            log.info("Add Attachment, attachment's id is : {}", attachment.getId());
            attachmentCache.put(attachment.getId(), attachment);
            tx.commit();
        }
    }
}
