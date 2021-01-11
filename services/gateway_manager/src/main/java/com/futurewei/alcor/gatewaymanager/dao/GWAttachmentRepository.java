package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class GWAttachmentRepository implements ICacheRepository<GWAttachment> {

    private final ICache<String, GWAttachment> cache;
    private final ICache<String, GatewayInfo> gatewayInfoCache;

    @Autowired
    public GWAttachmentRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(GWAttachment.class);
        this.gatewayInfoCache = cacheFactory.getCache(GatewayInfo.class);
    }

    @Override
    public GWAttachment findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, GWAttachment> findAllItems() throws CacheException {
        return null;
    }

    @Override
    public Map<String, GWAttachment> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return null;
    }

    @Override
    public void addItem(GWAttachment newItem) throws CacheException {
        log.debug("add GWAttachment, GWAttachment id is {}", newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void addItems(List<GWAttachment> items) throws CacheException {

    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("delete GWAttachment, GWAttachment id is {}", id);
        cache.remove(id);
    }

    public void deleteItem(String attachId, GatewayInfo gatewayInfo) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            gatewayInfoCache.put(gatewayInfo.getResourceId(),gatewayInfo);
            cache.remove(attachId);
            tx.commit();
        }
    }
}
