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
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class PortRepository {
    private static final Logger LOG = LoggerFactory.getLogger(PortRepository.class);

    private ICache<String, PortEntity> portCache;
    private ICache<String, PortNeighbors> neighborCache;
    private CacheFactory cacheFactory;
    private NeighborRepository neighborRepository;
    private SubnetPortsRepository subnetPortsRepository;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.portCache = cacheFactory.getCache(PortEntity.class);
        this.neighborCache= cacheFactory.getCache(PortNeighbors.class);

        this.neighborRepository = new NeighborRepository(cacheFactory);
        this.subnetPortsRepository = new SubnetPortsRepository(cacheFactory);
    }

    public PortRepository(ICache<String, PortEntity> portCache, ICache<String, PortNeighbors> neighborCache) {
        this.portCache = portCache;
        this.neighborCache= neighborCache;
    }

    @PostConstruct
    private void init() {
        LOG.info("PortRepository init done");
    }

    @DurationStatistics
    public void addPortEntities(List<PortEntity> portEntities) throws CacheException {
        Map<String, PortEntity> portEntityMap = portEntities
                .stream()
                .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
        portCache.putAll(portEntityMap);
    }

    @DurationStatistics
    public PortEntity findPortEntity(String portId) throws CacheException {
        return portCache.get(portId);
    }

    @DurationStatistics
    public Map<String, PortEntity> findAllPortEntities() throws CacheException {
        return portCache.getAll();
    }

    @DurationStatistics
    public Map<String, PortEntity> findAllPortEntities(Map<String, Object[]> queryParams) throws CacheException {
        return portCache.getAll(queryParams);
    }

    @DurationStatistics
    public synchronized void createPortAndNeighbor(PortEntity portEntity, NeighborInfo neighborInfo) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            //Add portEntity to portCache
            portCache.put(portEntity.getId(), portEntity);

            //Add neighborInfo to neighborCache
            if (neighborInfo != null) {
                PortNeighbors portNeighbors = neighborCache.get(portEntity.getVpcId());
                if (portNeighbors == null) {
                    portNeighbors = new PortNeighbors();
                    portNeighbors.setVpcId(portEntity.getVpcId());
                    portNeighbors.setNeighbors(new HashMap<>());
                }

                portNeighbors.getNeighbors().put(portEntity.getId(), neighborInfo);
                neighborCache.put(portEntity.getVpcId(), portNeighbors);
            }

            tx.commit();
        }
    }

    @DurationStatistics
    @Deprecated
    public synchronized void updatePortAndNeighbor(PortEntity portEntity, NeighborInfo neighborInfo) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            //Update portEntity to portCache
            portCache.put(portEntity.getId(), portEntity);

            //Update neighborInfo to neighborCache
            PortNeighbors portNeighbors = neighborCache.get(portEntity.getVpcId());
            if (portNeighbors == null) {
                if (neighborInfo != null) {
                    portNeighbors = new PortNeighbors();
                    portNeighbors.setVpcId(portEntity.getVpcId());
                    portNeighbors.setNeighbors(new HashMap<>());

                    portNeighbors.getNeighbors().put(neighborInfo.getPortId(), neighborInfo);
                    neighborCache.put(portEntity.getVpcId(), portNeighbors);
                }
            } else {
                if (neighborInfo == null) {
                    portNeighbors.getNeighbors().remove(portEntity.getId());
                } else {
                    portNeighbors.getNeighbors().replace(neighborInfo.getPortId(), neighborInfo);
                }

                neighborCache.put(portEntity.getVpcId(), portNeighbors);
            }

            tx.commit();
        }
    }

    @DurationStatistics
    @Deprecated
    public synchronized void createPortAndNeighborBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            //Add portEntities to portCache
            Map<String, PortEntity> portEntityMap = portEntities
                    .stream()
                    .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
            portCache.putAll(portEntityMap);

            //Add neighborInfos to neighborCache
            if (neighbors != null) {
                for (Map.Entry<String, List<NeighborInfo>> entry : neighbors.entrySet()) {
                    String vpcId = entry.getKey();
                    PortNeighbors portNeighbors = neighborCache.get(vpcId);
                    if (portNeighbors == null) {
                        portNeighbors = new PortNeighbors();
                        portNeighbors.setVpcId(vpcId);
                        portNeighbors.setNeighbors(new HashMap<>());
                    }

                    for (NeighborInfo neighborInfo : entry.getValue()) {
                        portNeighbors.getNeighbors().put(neighborInfo.getPortId(), neighborInfo);
                    }

                    neighborCache.put(vpcId, portNeighbors);
                }
            }

            tx.commit();
        }
    }

    @DurationStatistics
    @Deprecated
    public synchronized void updatePortAndNeighborBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            //Update portEntities to portCache
            Map<String, PortEntity> portEntityMap = portEntities
                    .stream()
                    .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
            portCache.putAll(portEntityMap);

            Map<String, List<PortEntity>> vpcPorts = new HashMap<>();
            for (PortEntity portEntity : portEntities) {
                if (!vpcPorts.containsKey(portEntity.getVpcId())) {
                    List<PortEntity> ports = new ArrayList<>();
                    vpcPorts.put(portEntity.getVpcId(), ports);
                }

                vpcPorts.get(portEntity.getVpcId()).add(portEntity);
            }

            //Delete old neighborInfos from neighborCache
            for (Map.Entry<String, List<PortEntity>> entry : vpcPorts.entrySet()) {
                PortNeighbors portNeighbors = neighborCache.get(entry.getKey());
                if (portNeighbors != null) {
                    for (PortEntity portEntity : entry.getValue()) {
                        portNeighbors.getNeighbors().remove(portEntity.getId());
                    }
                }

                neighborCache.put(entry.getKey(), portNeighbors);
            }

            //Add neighborInfos to neighborCache
            if (neighbors != null) {
                for (Map.Entry<String, List<NeighborInfo>> entry : neighbors.entrySet()) {
                    PortNeighbors portNeighbors = neighborCache.get(entry.getKey());
                    if (portNeighbors == null) {
                        portNeighbors = new PortNeighbors();
                        portNeighbors.setVpcId(entry.getKey());
                        portNeighbors.setNeighbors(new HashMap<>());
                    }

                    for (NeighborInfo neighborInfo : entry.getValue()) {
                        portNeighbors.getNeighbors().put(neighborInfo.getPortId(), neighborInfo);
                    }

                    neighborCache.put(entry.getKey(), portNeighbors);
                }
            }

            tx.commit();
        }

    }

    @DurationStatistics
    @Deprecated
    public synchronized void deletePortAndNeighbor(PortEntity portEntity) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            //Delete portEntity from portCache
            String portId = portEntity.getId();
            portCache.remove(portId);

            //Delete neighborInfo from neighborCache
            PortNeighbors portNeighbors = neighborCache.get(portEntity.getVpcId());
            if (portNeighbors != null && portNeighbors.getNeighbors().containsKey(portId)) {
                portNeighbors.getNeighbors().remove(portId);
                neighborCache.put(portNeighbors.getVpcId(), portNeighbors);
            }

            tx.commit();
        }
    }

    @DurationStatistics
    @Deprecated
    public PortNeighbors getPortNeighbors(Object arg) throws CacheException {
        String vpcId = (String) arg;
        PortNeighbors portNeighbors = neighborCache.get(vpcId);
        if (portNeighbors == null) {
            portNeighbors = new PortNeighbors(vpcId, null);
        }

        return portNeighbors;
    }

    @DurationStatistics
    public synchronized void createPortBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            Map<String, PortEntity> portEntityMap = portEntities
                    .stream()
                    .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
            portCache.putAll(portEntityMap);
            neighborRepository.createNeighbors(neighbors);
            subnetPortsRepository.addSubnetPortIds(portEntities);
            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void updatePort(PortEntity oldPortEntity, PortEntity newPortEntity, List<NeighborInfo> neighborInfos) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.put(newPortEntity.getId(), newPortEntity);
            neighborRepository.updateNeighbors(oldPortEntity, neighborInfos);
            subnetPortsRepository.updateSubnetPortIds(oldPortEntity, newPortEntity);
            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deletePort(PortEntity portEntity) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.remove(portEntity.getId());
            neighborRepository.deleteNeighbors(portEntity);
            subnetPortsRepository.deleteSubnetPortIds(portEntity);
            tx.commit();
        }
    }

    @DurationStatistics
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        return neighborRepository.getNeighbors(vpcId);
    }

    @DurationStatistics
    public int getSubnetPortCount(String subnetId) throws CacheException {
        return subnetPortsRepository.getSubnetPortNumber(subnetId);
    }

    @DurationStatistics
    public void createSubnetCache(String subnetId) throws CacheException {
        subnetPortsRepository.createSubnetCache(subnetId);
    }
}
