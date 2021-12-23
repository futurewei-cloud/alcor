/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(prefix = "protobuf.goal-state-message", name = "version", havingValue = "102")
public class PortRepositoryV2 implements IPortRepository {
    private static final Logger LOG = LoggerFactory.getLogger(PortRepository.class);

    private ICache<String, PortEntity> portCache;
    private CacheFactory cacheFactory;
    private SubnetPortsRepository subnetPortsRepository;

    @Autowired
    public PortRepositoryV2(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.portCache = cacheFactory.getCache(PortEntity.class);

        this.subnetPortsRepository = new SubnetPortsRepository(cacheFactory);
    }

    public PortRepositoryV2(ICache<String, PortEntity> portCache, ICache<String, PortNeighbors> neighborCache) {
        this.portCache = portCache;
    }

    @PostConstruct
    private void init() {
        LOG.info("PortRepository init done");
    }

    @DurationStatistics
    @Override
    public void addPortEntities(List<PortEntity> portEntities) throws CacheException {
        Map<String, PortEntity> portEntityMap = portEntities
                .stream()
                .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
        portCache.putAll(portEntityMap);
    }

    @DurationStatistics
    @Override
    public PortEntity findPortEntity(String portId) throws CacheException {
        return portCache.get(portId);
    }

    @DurationStatistics
    @Override
    public Map<String, PortEntity> findAllPortEntities() throws CacheException {
        return portCache.getAll();
    }

    @DurationStatistics
    @Override
    public Map<String, PortEntity> findAllPortEntities(Map<String, Object[]> queryParams) throws CacheException {
        return portCache.getAll(queryParams);
    }

    @DurationStatistics
    @Override
    public synchronized void createPortBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            Map<String, PortEntity> portEntityMap = portEntities
                    .stream()
                    .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
            portCache.putAll(portEntityMap);
            subnetPortsRepository.addSubnetPortIds(portEntities);
            tx.commit();
        }
    }

    @DurationStatistics
    @Override
    public synchronized void updatePort(PortEntity oldPortEntity, PortEntity newPortEntity, List<NeighborInfo> neighborInfos) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.put(newPortEntity.getId(), newPortEntity);
            subnetPortsRepository.updateSubnetPortIds(oldPortEntity, newPortEntity);
            tx.commit();
        }
    }

    @DurationStatistics
    @Override
    public synchronized void deletePort(PortEntity portEntity) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.remove(portEntity.getId());
            subnetPortsRepository.deleteSubnetPortIds(portEntity);
            tx.commit();
        }
    }

    @DurationStatistics
    @Override
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        return new HashMap<>();
    }

    @DurationStatistics
    @Override
    public int getSubnetPortCount(String subnetId) throws CacheException {
        return subnetPortsRepository.getSubnetPortNumber(subnetId);
    }
}
