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
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentScan(value = "com.futurewei.alcor.common.db")
@Repository
public class PortRepository {
    private static final Logger LOG = LoggerFactory.getLogger(PortRepository.class);

    private ICache<String, PortEntity> portCache;
    private ICache<String, PortNeighbors> neighborCache;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        portCache = cacheFactory.getCache(PortEntity.class);
        neighborCache= cacheFactory.getCache(PortNeighbors.class);
    }

    public PortRepository(ICache<String, PortEntity> portCache, ICache<String, PortNeighbors> neighborCache) {
        this.portCache = portCache;
        this.neighborCache= neighborCache;
    }

    @PostConstruct
    private void init() {
        LOG.info("PortRepository init done");
    }

    public PortEntity findPortEntity(String portId) throws CacheException {
        return portCache.get(portId);
    }

    public Map<String, PortEntity> findAllPortEntities() throws CacheException {
        return portCache.getAll();
    }

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

    public PortNeighbors getPortNeighbors(Object arg) throws CacheException {
        String vpcId = (String) arg;
        PortNeighbors portNeighbors = neighborCache.get(vpcId);
        if (portNeighbors == null) {
            portNeighbors = new PortNeighbors(vpcId, null);
        }

        return portNeighbors;
    }
}
