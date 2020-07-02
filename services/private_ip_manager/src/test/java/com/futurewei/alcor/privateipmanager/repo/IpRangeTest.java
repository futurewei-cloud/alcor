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
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.privateipmanager.entity.VpcIpRange;
import com.futurewei.alcor.privateipmanager.exception.IpAddrRangeExistException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrRangeNotFoundException;
import com.futurewei.alcor.web.entity.ip.IpAddrRangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.futurewei.alcor.privateipmanager.util.IpAddressBuilder.buildIpAddrRangeRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IpRangeTest {
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
    }

    @Test
    public void createIpAddrRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = buildIpAddrRangeRequest();
        ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);
        assertEquals(ipAddrRangeRequest.getTotalIps(), 254);
        assertEquals(ipAddrRangeRequest.getUsedIps(), 0);

        VpcIpRange vpcIpRange = vpcIpRangeCache.get(UnitTestConfig.vpcId);
        assertNotNull(vpcIpRange);
        assertEquals(vpcIpRange.getVpcId(), UnitTestConfig.vpcId);
        assertEquals(vpcIpRange.getRanges().size(), 1);
        assertEquals(vpcIpRange.getRanges().get(0), UnitTestConfig.rangeId);
    }

    @Test
    public void createDuplicateIpAddrRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = buildIpAddrRangeRequest();
        ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);

        assertThrows(IpAddrRangeExistException.class, ()-> {
            ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);
        }, "Expected throw IpAddrRangeExistException, but it didn't");
    }

    @Test
    public void deleteExistIpAddrRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = buildIpAddrRangeRequest();
        ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);
        ipAddrRangeRepo.deleteIpAddrRange(UnitTestConfig.rangeId);

        IpAddrRange ipAddrRange = ipAddrRangeRepo.getIpAddrRange(UnitTestConfig.rangeId);
        assertNull(ipAddrRange);

        VpcIpRange vpcIpRange = vpcIpRangeCache.get(UnitTestConfig.vpcId);
        assertNull(vpcIpRange);
    }

    @Test
    public void deleteNotExistIpAddrRangeTest() {
        assertThrows(IpAddrRangeNotFoundException.class, ()-> {
            ipAddrRangeRepo.deleteIpAddrRange(UnitTestConfig.rangeId);
        }, "Expected throw IpAddrRangeNotFoundException, but it didn't");
    }

    @Test
    public void getExistIpRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = buildIpAddrRangeRequest();
        ipAddrRangeRepo.createIpAddrRange(ipAddrRangeRequest);
        IpAddrRange ipAddrRange = ipAddrRangeRepo.getIpAddrRange(UnitTestConfig.rangeId);
        assertNotNull(ipAddrRange);
        assertEquals(ipAddrRangeRequest.getTotalIps(), 254);
        assertEquals(ipAddrRangeRequest.getUsedIps(), 0);
    }

    @Test
    public void getNotExistIpRangeTest() throws Exception {
        IpAddrRange ipAddrRange = ipAddrRangeRepo.getIpAddrRange(UnitTestConfig.rangeId);
        assertNull(ipAddrRange);
    }
}
