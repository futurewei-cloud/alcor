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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.restclient.*;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static com.futurewei.alcor.portmanager.controller.ResourceBuilder.*;

public class MockRestClientAndRepository {
    @MockBean
    VpcManagerRestClient vpcManagerRestClient;

    @MockBean
    private SubnetManagerRestClient subnetManagerRestClient;

    @MockBean
    private IpManagerRestClient ipManagerRestClient;

    @MockBean
    private MacManagerRestClient macManagerRestClient;

    @MockBean
    private NodeManagerRestClient nodeManagerRestClient;

    @MockBean
    private RouteManagerRestClient routeManagerRestClient;

    @MockBean
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    @MockBean
    private SecurityGroupManagerRestClient securityGroupManagerRestClient;

    @MockBean
    private PortRepository portRepository;

    protected void mockRestClientsAndRepositoryOperations() throws Exception {
        Mockito.when(vpcManagerRestClient.getVpc(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(newVpcStateJson());

        Mockito.when(subnetManagerRestClient.getSubnet(UnitTestConfig.projectId, UnitTestConfig.subnetId))
                .thenReturn(newSubnetStateJson());

        Mockito.when(ipManagerRestClient.allocateIpAddress(null, null, UnitTestConfig.rangeId, UnitTestConfig.ip1))
                .thenReturn(newIpv4AddrRequest());

        Mockito.when(ipManagerRestClient.allocateIpAddress(IpVersion.IPV4, UnitTestConfig.vpcId, null, null))
                .thenReturn(newIpv4AddrRequest());

        Mockito.when(ipManagerRestClient.allocateIpAddress(IpVersion.IPV6, UnitTestConfig.vpcId, null, null))
                .thenReturn(newIpv6AddrRequest());

        Mockito.when(macManagerRestClient.allocateMacAddress(UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.portId, null))
                .thenReturn(newMacStateJson(UnitTestConfig.portId, UnitTestConfig.mac1));

        Mockito.when(macManagerRestClient.allocateMacAddress(UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.portId2, null))
                .thenReturn(newMacStateJson(UnitTestConfig.portId2, UnitTestConfig.mac2));

        Mockito.when(macManagerRestClient.allocateMacAddress(UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.portId, UnitTestConfig.mac1))
                .thenReturn(newMacStateJson(UnitTestConfig.portId, UnitTestConfig.mac1));

        Mockito.when(routeManagerRestClient.getRouteBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(newRouteWebJson());

        Mockito.when(securityGroupManagerRestClient.getSecurityGroup(UnitTestConfig.projectId, UnitTestConfig.securityGroupId))
                .thenReturn(newSecurityGroupWebJson());

        Mockito.when(securityGroupManagerRestClient.getDefaultSecurityGroup(UnitTestConfig.projectId))
                .thenReturn(newDefaultSecurityGroupWebJson());

        Mockito.when(nodeManagerRestClient.getNodeInfo(UnitTestConfig.nodeId))
                .thenReturn(newNodeInfoJson(UnitTestConfig.nodeId, UnitTestConfig.ip1));

        Mockito.when(nodeManagerRestClient.getNodeInfo(UnitTestConfig.nodeId2))
                .thenReturn(newNodeInfoJson(UnitTestConfig.nodeId2, UnitTestConfig.ip2));

        Mockito.when(portRepository.findItem(UnitTestConfig.portId))
                .thenReturn(newPortStateJson(UnitTestConfig.portId).getPortEntity());

        Mockito.when(portRepository.findItem(UnitTestConfig.portId2))
                .thenReturn(newPortStateJson(UnitTestConfig.portId2).getPortEntity());

        Map<String, PortEntity> portStates = new HashMap<>();
        portStates.put(UnitTestConfig.portId, newPortStateJson(UnitTestConfig.portId).getPortEntity());

        Mockito.when(portRepository.findAllItems())
                .thenReturn(portStates);
    }
}
