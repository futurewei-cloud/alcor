/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.repo;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.port.SubnetPortIds;
import com.futurewei.alcor.portmanager.exception.FixedIpsInvalid;
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
    private static final String NEIGHBOR_CACHE_NAME_PREFIX = "neighborCache-";

    private ICache<String, PortEntity> portCache;
    private ICache<String, PortNeighbors> neighborCache;
    private ICache<String, SubnetPortIds> subnetPortIdsCache;
    private CacheFactory cacheFactory;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        portCache = cacheFactory.getCache(PortEntity.class);
        neighborCache= cacheFactory.getCache(PortNeighbors.class);
        subnetPortIdsCache = cacheFactory.getCache(SubnetPortIds.class);
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

    private String getNeighborCacheName(String suffix) {
        return NEIGHBOR_CACHE_NAME_PREFIX + suffix;
    }

    private List<SubnetPortIds> getSubnetPortIds(List<PortEntity> portEntities) {
        Map<String, SubnetPortIds> subnetPortIdsMap = new HashMap<>();
        for (PortEntity portEntity: portEntities) {
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps == null) {
                LOG.warn("Port:{} has no ip address", portEntity.getId());
                continue;
            }

            for (PortEntity.FixedIp fixedIp: fixedIps) {
                String subnetId = fixedIp.getSubnetId();
                if (!subnetPortIdsMap.containsKey(subnetId)) {
                    SubnetPortIds subnetPortIds = new SubnetPortIds(subnetId, new HashSet<>());
                    subnetPortIdsMap.put(subnetId, subnetPortIds);
                }

                subnetPortIdsMap.get(subnetId).getPortIds().add(portEntity.getId());
            }
        }

        return new ArrayList<>(subnetPortIdsMap.values());
    }

    @DurationStatistics
    public synchronized void createPortBulk(List<PortEntity> portEntities, Map<String, List<NeighborInfo>> neighbors) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            Map<String, PortEntity> portEntityMap = portEntities
                    .stream()
                    .collect(Collectors.toMap(PortEntity::getId, Function.identity()));
            portCache.putAll(portEntityMap);

            for (Map.Entry<String, List<NeighborInfo>> entry : neighbors.entrySet()) {
                Map<String, NeighborInfo> neighborMap = entry.getValue()
                        .stream()
                        .collect(Collectors.toMap(NeighborInfo::getPortId, Function.identity()));

                ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                        NeighborInfo.class, getNeighborCacheName(entry.getKey()));
                neighborCache.putAll(neighborMap);
            }

            //Store the mapping between subnet id and port id
            List<SubnetPortIds> subnetPortIdsList = getSubnetPortIds(portEntities);

            for (SubnetPortIds item: subnetPortIdsList) {
                String subnetId = item.getSubnetId();
                Set<String> portIds = item.getPortIds();

                SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                if (subnetPortIds == null) {
                    subnetPortIds = new SubnetPortIds(subnetId, new HashSet<>(portIds));
                } else {
                    subnetPortIds.getPortIds().addAll(portIds);
                }

                subnetPortIdsCache.put(subnetId, subnetPortIds);
            }

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void updatePort(PortEntity oldPortEntity, PortEntity newPortEntity, NeighborInfo neighborInfo) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.put(newPortEntity.getId(), newPortEntity);

            ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                    NeighborInfo.class, getNeighborCacheName(newPortEntity.getVpcId()));
            if (neighborInfo != null) {
                neighborCache.put(newPortEntity.getId(), neighborInfo);
            } else {
                neighborCache.remove(newPortEntity.getId());
            }

            if (oldPortEntity.getFixedIps() == null || newPortEntity.getFixedIps() == null) {
                LOG.error("Can not find fixed ip in port entity");
                throw new FixedIpsInvalid();
            }

            List<String> oldSubnetIds = oldPortEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getSubnetId)
                    .collect(Collectors.toList());

            List<String> newSubnetIds = oldPortEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getSubnetId)
                    .collect(Collectors.toList());

            if (!oldSubnetIds.equals(newSubnetIds)) {
                //Delete old items from subnetPortIdsCache
                for (String subnetId: oldSubnetIds) {
                    SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                    if (subnetPortIds != null) {
                        subnetPortIds.getPortIds().remove(oldPortEntity.getId());
                        subnetPortIdsCache.put(subnetId, subnetPortIds);
                    }
                }

                //Add new items to subnetPortIdsCache
                for (String subnetId: newSubnetIds) {
                    SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                    if (subnetPortIds != null) {
                        subnetPortIds.getPortIds().add(newPortEntity.getId());
                    } else {
                        Set<String> portIds = new HashSet<>();
                        portIds.add(newPortEntity.getId());
                        subnetPortIds = new SubnetPortIds(subnetId, portIds);
                    }
                    subnetPortIdsCache.put(subnetId, subnetPortIds);
                }
            }

            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deletePort(PortEntity portEntity) throws Exception {
        try (Transaction tx = portCache.getTransaction().start()) {
            portCache.remove(portEntity.getId());

            ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                    NeighborInfo.class, getNeighborCacheName(portEntity.getVpcId()));
            neighborCache.remove(portEntity.getId());

            if (portEntity.getFixedIps() == null) {
                LOG.error("Can not find fixed ip in port entity");
                throw new FixedIpsInvalid();
            }

            List<String> oldSubnetIds = portEntity.getFixedIps().stream()
                    .map(PortEntity.FixedIp::getSubnetId)
                    .collect(Collectors.toList());

            //Delete old items from subnetPortIdsCache
            for (String subnetId: oldSubnetIds) {
                SubnetPortIds subnetPortIds = subnetPortIdsCache.get(subnetId);
                if (subnetPortIds != null) {
                    subnetPortIds.getPortIds().remove(portEntity.getId());
                    subnetPortIdsCache.put(subnetId, subnetPortIds);
                }
            }

            tx.commit();
        }
    }

    @DurationStatistics
    public Map<String, NeighborInfo> getNeighbors(String vpcId) throws CacheException {
        ICache<String, NeighborInfo> neighborCache = this.cacheFactory.getCache(
                NeighborInfo.class, getNeighborCacheName(vpcId));
        return neighborCache.getAll();
    }

    @DurationStatistics
    public SubnetPortIds getSubnetPortIds(String subnetId) throws CacheException {
        return subnetPortIdsCache.get(subnetId);
    }
}
