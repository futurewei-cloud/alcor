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
package com.futurewei.alcor.privateipmanager.repo;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.MockCache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.privateipmanager.config.UnitTestConfig;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import com.futurewei.alcor.privateipmanager.exception.IpAddrAllocNotFoundException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrNotEnoughException;
import com.futurewei.alcor.privateipmanager.exception.IpRangeNotFoundException;
import com.futurewei.alcor.privateipmanager.exception.NotFoundIpRangeFromVpc;
import com.futurewei.alcor.web.entity.ip.IpAddrRangeRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.futurewei.alcor.privateipmanager.util.IpAddressBuilder.buildIpAddrRangeRequest;
import static com.futurewei.alcor.privateipmanager.util.IpAddressBuilder.buildIpAddrRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IpAddrTest {
    private IpAddrRangeRepo ipAddrRangeRepo;
    private ICache<String, IpAddrRange> ipAddrRangeCache;
    private ICache<String, VpcIpRange> vpcIpRangeCache;
    private Transaction transaction;

    @BeforeEach
    public void beforeEachTest() throws Exception {
        transaction = mock(Transaction.class);
        ipAddrRangeCache = spy(new MockCache<>(transaction));
        vpcIpRangeCache = spy(new MockCache<>(transaction));

        ipAddrRangeRepo = new IpAddrRangeRepo(ipAddrRangeCache, vpcIpRangeCache);

        when(transaction.start()).thenReturn(transaction);

        IpAddrRangeRequest ipAddrRangeRequest = buildIpAddrRangeRequest();
        ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);
    }

    @Test
    public void allocateIpAddrWithIpAddrTest() throws Exception {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.activated);
    }

    @Test
    public void allocateIpAddrWithoutIpAddrTest() throws Exception {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        ipAddrRequest.setIp(null);
        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.activated);
    }

    @Test
    public void allocateIpAddrWithoutRangeIdTest() throws Exception {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        ipAddrRequest.setRangeId(null);
        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.activated);
    }

    @Test
    public void allocateIpAddrWithWrongRangeIdTest() {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        ipAddrRequest.setRangeId("wrongRangeId");

        assertThrows(IpRangeNotFoundException.class, ()-> {
            ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        }, "Expected throw IpRangeNotFoundException, but it didn't");
    }

    @Test
    public void allocateIpAddrWithWrongVpcIdTest() {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        ipAddrRequest.setRangeId(null);
        ipAddrRequest.setVpcId("wrongVpcId");

        assertThrows(NotFoundIpRangeFromVpc.class, ()-> {
            ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        }, "Expected throw NotFoundIpRangeFromVpc, but it didn't");
    }

    @Test
    public void allocateIpAddrWithWrongIpVersionTest() {
        IpAddrRequest ipAddrRequest = buildIpAddrRequest();
        ipAddrRequest.setRangeId(null);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv6);

        assertThrows(IpAddrNotEnoughException.class, ()-> {
            ipAddrRangeRepo.allocateIpAddr(ipAddrRequest);
        }, "Expected throw IpAddrNotEnoughException, but it didn't");
    }

    @Test
    public void deactivatedExistIpAddrTest() throws Exception {
        allocateIpAddrWithIpAddrTest();

        ipAddrRangeRepo.modifyIpAddrState(UnitTestConfig.rangeId,
                UnitTestConfig.ip1,
                UnitTestConfig.deactivated);

        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(UnitTestConfig.rangeId, UnitTestConfig.ip1);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.deactivated);
    }

    @Test
    public void deactivatedNotExistIpAddrTest() {
        assertThrows(IpAddrAllocNotFoundException.class, ()-> {
            ipAddrRangeRepo.modifyIpAddrState(UnitTestConfig.rangeId,
                    UnitTestConfig.ip1,
                    UnitTestConfig.deactivated);
        }, "Expected throw IpAddrAllocNotFoundException, but it didn't");
    }

    @Test
    public void releaseExistIpAddrTest() throws Exception {
        allocateIpAddrWithIpAddrTest();
        ipAddrRangeRepo.releaseIpAddr(UnitTestConfig.rangeId,
                UnitTestConfig.ip1);

        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(UnitTestConfig.rangeId, UnitTestConfig.ip1);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.free);
    }

    @Test
    public void releaseNotExistIpAddrTest() {
        assertThrows(IpAddrAllocNotFoundException.class, ()-> {
            ipAddrRangeRepo.releaseIpAddr(UnitTestConfig.rangeId,
                    UnitTestConfig.ip1);
        }, "Expected throw IpAddrAllocNotFoundException, but it didn't");
    }

    @Test
    public void getExistIpAddrTest() throws Exception {
        allocateIpAddrWithIpAddrTest();
        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(UnitTestConfig.rangeId, UnitTestConfig.ip1);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.activated);
    }

    @Test
    public void getNotExistIpAddrTest() throws Exception {
        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(UnitTestConfig.rangeId, UnitTestConfig.ip1);
        assertNotNull(ipAddrAlloc);
        assertEquals(ipAddrAlloc.getIpAddr(), UnitTestConfig.ip1);
        assertEquals(ipAddrAlloc.getState(), UnitTestConfig.free);
    }
}
