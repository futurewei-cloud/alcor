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
package com.futurewei.alcor.networkaclmanager.repo;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.networkaclmanager.config.UnitTestConfig;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclBuilder;
import com.futurewei.alcor.networkaclmanager.util.NetworkAclRuleBuilder;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class NetworkAclRepositoryTest {
    private static NetworkAclRepository networkAclRepository;
    private static ICache<String, NetworkAclEntity> networkAclCache;
    private static ICache<String, NetworkAclRuleEntity> networkAclRuleCache;

    @BeforeAll
    public static void beforeAllTestCases() {
        networkAclCache = mock(ICache.class);
        networkAclRuleCache = mock(ICache.class);
        networkAclRepository = new NetworkAclRepository(networkAclCache, networkAclRuleCache);
    }

    @Test
    public void addNetworkAclTest() throws Exception {
        networkAclRepository.addNetworkAcl(NetworkAclBuilder.buildNetworkAclEntity1());

        Mockito.verify(networkAclCache, Mockito.times(1))
                .put(UnitTestConfig.networkAclId1, NetworkAclBuilder.buildNetworkAclEntity1());
    }

    @Test
    public void deleteNetworkAclTest() throws Exception {
        networkAclRepository.deleteNetworkAcl(UnitTestConfig.networkAclId1);

        Mockito.verify(networkAclCache, Mockito.times(1))
                .remove(UnitTestConfig.networkAclId1);
    }

    @Test
    public void getNetworkAclTest() throws Exception {
        Mockito.when(networkAclCache.get(UnitTestConfig.networkAclId1))
                .thenReturn(NetworkAclBuilder.buildNetworkAclEntity1());

        assertNotNull(networkAclRepository.getNetworkAcl(UnitTestConfig.networkAclId1));
    }

    @Test
    public void listNetworkAclTest() throws Exception {
        Map<String, NetworkAclEntity> networkAclEntityMap = new HashMap<>();
        networkAclEntityMap.put(UnitTestConfig.networkAclId1, NetworkAclBuilder.buildNetworkAclEntity1());
        Mockito.when(networkAclCache.getAll())
                .thenReturn(networkAclEntityMap);

        Map<String, NetworkAclEntity> allNetworkAcls = networkAclRepository.getAllNetworkAcls();
        assertEquals(allNetworkAcls.size(), 1);
    }

    @Test
    public void addNetworkAclRuleTest() throws Exception {
        networkAclRepository.addNetworkAclRule(NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());

        Mockito.verify(networkAclRuleCache, Mockito.times(1))
                .put(UnitTestConfig.networkAclRuleId1, NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());
    }

    @Test
    public void deleteNetworkAclRuleTest() throws Exception {
        networkAclRepository.deleteNetworkAclRule(UnitTestConfig.networkAclRuleId1);

        Mockito.verify(networkAclRuleCache, Mockito.times(1))
                .remove(UnitTestConfig.networkAclRuleId1);
    }

    @Test
    public void getNetworkAclRuleTest() throws Exception {
        Mockito.when(networkAclRuleCache.get(UnitTestConfig.networkAclRuleId1))
                .thenReturn(NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());

        assertNotNull(networkAclRepository.getNetworkAclRule(UnitTestConfig.networkAclRuleId1));
    }

    @Test
    public void listNetworkAclRuleTest() throws Exception {
        Map<String, NetworkAclRuleEntity> networkAclRuleEntityMap = new HashMap<>();
        networkAclRuleEntityMap.put(UnitTestConfig.networkAclRuleId1, NetworkAclRuleBuilder.buildNetworkAclRuleEntity1());
        Mockito.when(networkAclRuleCache.getAll())
                .thenReturn(networkAclRuleEntityMap);

        Map<String, NetworkAclRuleEntity> allNetworkAclRules = networkAclRepository.getAllNetworkAclRules();
        assertEquals(allNetworkAclRules.size(), 1);
    }

    @Test
    public void getNetworkAclRulesByNumberTest() throws Exception {
        //FIXME:not support yet!!!
    }

    @Test
    public void getNetworkAclRulesByNetworkAclIdTest() throws Exception {
        //FIXME:not support yet!!!
    }
}
