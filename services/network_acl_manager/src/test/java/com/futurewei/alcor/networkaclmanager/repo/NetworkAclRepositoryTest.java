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
package com.futurewei.alcor.networkaclmanager.repo;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class NetworkAclRepositoryTest {
    private static NetworkAclRepository networkAclRepository;
    private static ICache networkAclCache;
    private static ICache networkAclRuleCache;

    @BeforeAll
    public static void beforeAllTestCases() {
        networkAclCache = mock(ICache.class);
        networkAclRuleCache = mock(ICache.class);
        networkAclRepository = new NetworkAclRepository(networkAclCache, networkAclRuleCache);
    }

    @Test
    public void addNetworkAclTest() throws Exception {
        networkAclRepository.addNetworkAcl(NetworkAclBuilder.buildNetworkAclEntity());

        Mockito.verify(networkAclCache, Mockito.times(1))
                .put(UnitTestConfig.networkAclId, NetworkAclBuilder.buildNetworkAclEntity());
    }

    @Test
    public void deleteNetworkAclTest() throws Exception {
        networkAclRepository.deleteNetworkAcl(UnitTestConfig.networkAclId);

        Mockito.verify(networkAclCache, Mockito.times(1))
                .remove(UnitTestConfig.networkAclId);
    }

    @Test
    public void getNetworkAclTest() throws Exception {
        Mockito.when(networkAclCache.get(UnitTestConfig.networkAclId))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity());

        assertNotNull(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId));
    }

    @Test
    public void listNetworkAclTest() throws Exception {
        Map<String, NetworkAclEntity> networkAclEntityMap = new HashMap<>();
        networkAclEntityMap.put(UnitTestConfig.networkAclId, NetworkAclBuilder.buildNetworkAclEntity());
        Mockito.when(networkAclCache.getAll())
                .thenReturn(networkAclEntityMap);

        Map<String, NetworkAclEntity> allNetworkAcls = networkAclRepository.getAllNetworkAcls();
        assertEquals(allNetworkAcls.size(), 1);
    }
}
