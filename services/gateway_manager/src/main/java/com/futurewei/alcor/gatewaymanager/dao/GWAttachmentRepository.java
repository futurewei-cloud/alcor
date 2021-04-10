/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.gatewaymanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GWAttachmentRepository implements ICacheRepository<GWAttachment> {

    private final ICache<String, GWAttachment> cache;
    private final ICache<String, GatewayEntity> gatewayEntityICache;

    @Autowired
    public GWAttachmentRepository(CacheFactory cacheFactory) {
        this.cache = cacheFactory.getCache(GWAttachment.class);
        this.gatewayEntityICache = cacheFactory.getCache(GatewayEntity.class);
    }

    @Override
    public GWAttachment findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, GWAttachment> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, GWAttachment> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(GWAttachment newItem) throws CacheException {
        log.debug("add GWAttachment, GWAttachment id is {}", newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void addItems(List<GWAttachment> items) throws CacheException {
        Map<String, GWAttachment> gwAttachmentMap = items.stream().collect(Collectors.toMap(GWAttachment::getId, Function.identity()));
        cache.putAll(gwAttachmentMap);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        log.debug("delete GWAttachment, GWAttachment id is {}", id);
        cache.remove(id);
    }

    public void deleteAndUpdateItem(String attachId, GatewayEntity gatewayEntity) throws Exception {
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(attachId);
            gatewayEntityICache.put(gatewayEntity.getId(),gatewayEntity);
            tx.commit();
        }
    }
}
