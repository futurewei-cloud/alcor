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
