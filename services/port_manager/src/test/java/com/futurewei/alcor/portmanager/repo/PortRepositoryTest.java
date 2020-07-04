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
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.futurewei.alcor.portmanager.util.ResourceBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

public class PortRepositoryTest {
    private PortRepository portRepository;
    private ICache<String, PortEntity> portCache;
    private ICache<String, PortNeighbors> neighborCache;
    private Transaction transaction;

    @BeforeEach
    public void beforeEachTest() throws CacheException {
        portCache = mock(ICache.class);
        neighborCache = mock(ICache.class);
        transaction = mock(Transaction.class);

        portRepository = new PortRepository(portCache, neighborCache);

        Mockito.when(portCache.getTransaction())
                .thenReturn(transaction);

        Mockito.when(transaction.start())
                .thenReturn(transaction);
    }

    @Test
    public void findPortEntityTest() throws CacheException {
        Mockito.when(portRepository.findPortEntity(UnitTestConfig.portId1))
                .thenReturn(buildPortEntity(UnitTestConfig.portId1));

        PortEntity portEntity = portRepository.findPortEntity(UnitTestConfig.portId1);
        assertEquals(portEntity.getId(), UnitTestConfig.portId1);
    }

    @Test
    public void findAllPortEntitiesTest() throws CacheException {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);
        Map<String, PortEntity> portEntityMap = new HashMap<>();
        portEntityMap.put(portEntity.getId(), portEntity);
        Mockito.when(portRepository.findAllPortEntities())
                .thenReturn(portEntityMap);

        Map<String, PortEntity> portEntities = portRepository.findAllPortEntities();
        assertEquals(portEntities.size(), 1);
    }

    @Test
    public void createPortAndNeighborTest1() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);
        NeighborInfo neighborInfo = buildNeighborInfo(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.createPortAndNeighbor(portEntity, neighborInfo);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void createPortAndNeighborTest2() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);
        NeighborInfo neighborInfo = buildNeighborInfo(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(null);

        portRepository.createPortAndNeighbor(portEntity, neighborInfo);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void createPortAndNeighborTest3() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.createPortAndNeighbor(portEntity, null);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void createPortAndNeighborTest4() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(null);

        portRepository.createPortAndNeighbor(portEntity, null);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborTest1() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);
        NeighborInfo neighborInfo = buildNeighborInfo(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.updatePortAndNeighbor(portEntity, neighborInfo);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborTest2() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);
        NeighborInfo neighborInfo = buildNeighborInfo(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(null);

        portRepository.updatePortAndNeighbor(portEntity, neighborInfo);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborTest3() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.updatePortAndNeighbor(portEntity, null);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborTest4() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(null);

        portRepository.updatePortAndNeighbor(portEntity, null);

        Mockito.verify(portCache, Mockito.times(1))
                .put(UnitTestConfig.portId1, portEntity);

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void createPortAndNeighborBulkTest1() throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        portEntities.add(buildPortEntity(UnitTestConfig.portId1));
        portEntities.add(buildPortEntity(UnitTestConfig.portId2));

        List<NeighborInfo> neighborInfos = new ArrayList<>();
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId1));
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId2));

        Map<String, List<NeighborInfo>> neighbors = new HashMap<>();
        neighbors.put(UnitTestConfig.vpcId, neighborInfos);

        portRepository.createPortAndNeighborBulk(portEntities, neighbors);

        Mockito.verify(portCache, Mockito.times(1))
                .putAll(anyMap());

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(UnitTestConfig.vpcId), any(PortNeighbors.class));
    }

    @Test
    public void createPortAndNeighborBulkTest2() throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        portEntities.add(buildPortEntity(UnitTestConfig.portId1));
        portEntities.add(buildPortEntity(UnitTestConfig.portId2));

        portRepository.createPortAndNeighborBulk(portEntities, null);

        Mockito.verify(portCache, Mockito.times(1))
                .putAll(anyMap());

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(UnitTestConfig.vpcId), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborBulkTest1() throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        portEntities.add(buildPortEntity(UnitTestConfig.portId1));
        portEntities.add(buildPortEntity(UnitTestConfig.portId2));

        List<NeighborInfo> neighborInfos = new ArrayList<>();
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId1));
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId2));

        Map<String, List<NeighborInfo>> neighbors = new HashMap<>();
        neighbors.put(UnitTestConfig.vpcId, neighborInfos);

        portRepository.updatePortAndNeighborBulk(portEntities, neighbors);

        Mockito.verify(portCache, Mockito.times(1))
                .putAll(anyMap());

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(UnitTestConfig.vpcId), isNull());

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(UnitTestConfig.vpcId), any(PortNeighbors.class));
    }

    @Test
    public void updatePortAndNeighborBulkTest2() throws Exception {
        List<PortEntity> portEntities = new ArrayList<>();
        portEntities.add(buildPortEntity(UnitTestConfig.portId1));
        portEntities.add(buildPortEntity(UnitTestConfig.portId2));

        List<NeighborInfo> neighborInfos = new ArrayList<>();
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId1));
        neighborInfos.add(buildNeighborInfo(UnitTestConfig.portId2));

        Map<String, List<NeighborInfo>> neighbors = new HashMap<>();
        neighbors.put(UnitTestConfig.vpcId, neighborInfos);

        Mockito.when(neighborCache.get(UnitTestConfig.vpcId))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.updatePortAndNeighborBulk(portEntities, neighbors);

        Mockito.verify(portCache, Mockito.times(1))
                .putAll(anyMap());

        Mockito.verify(neighborCache, Mockito.times(2))
                .put(eq(UnitTestConfig.vpcId), any(PortNeighbors.class));
    }

    @Test
    public void deletePortAndNeighborTest1() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        portRepository.deletePortAndNeighbor(portEntity);

        Mockito.verify(portCache, Mockito.times(1))
                .remove(UnitTestConfig.portId1);

        Mockito.verify(neighborCache, Mockito.times(1))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void deletePortAndNeighborTest2() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(null);

        portRepository.deletePortAndNeighbor(portEntity);

        Mockito.verify(portCache, Mockito.times(1))
                .remove(UnitTestConfig.portId1);

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void deletePortAndNeighborTest3() throws Exception {
        PortEntity portEntity = buildPortEntity(UnitTestConfig.portId1);

        Mockito.when(neighborCache.get(portEntity.getVpcId()))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId2));

        portRepository.deletePortAndNeighbor(portEntity);

        Mockito.verify(portCache, Mockito.times(1))
                .remove(UnitTestConfig.portId1);

        Mockito.verify(neighborCache, Mockito.times(0))
                .put(eq(portEntity.getVpcId()), any(PortNeighbors.class));
    }

    @Test
    public void getPortNeighborsTest() throws Exception {
        Mockito.when(neighborCache.get(UnitTestConfig.vpcId))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        PortNeighbors portNeighbors = portRepository.getPortNeighbors(UnitTestConfig.vpcId);

        assertEquals(portNeighbors.getVpcId(), UnitTestConfig.vpcId);
        assertEquals(portNeighbors.getNeighbors().size(), 1);
    }
}
