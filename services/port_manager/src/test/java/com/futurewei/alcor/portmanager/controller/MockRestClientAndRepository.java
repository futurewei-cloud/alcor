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
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.restclient.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.futurewei.alcor.portmanager.util.ResourceBuilder.*;
import static org.mockito.ArgumentMatchers.*;

public class MockRestClientAndRepository {
    @MockBean
    VpcManagerRestClient vpcManagerRestClient;

    @MockBean
    private SubnetManagerRestClient subnetManagerRestClient;

    @MockBean
    protected IpManagerRestClient ipManagerRestClient;

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
    private ElasticIpManagerRestClient elasticIpManagerRestClient;

    @MockBean
    private RouterManagerRestClient routerManagerRestClient;

    @MockBean
    private PortRepository portRepository;

    @BeforeEach
    protected void mockRestClientsAndRepositoryOperations() throws Exception {
        Mockito.when(vpcManagerRestClient.getVpc(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(buildVpcWebJson());

        Mockito.when(vpcManagerRestClient.getVpcBulk(anyString(), anyList()))
                .thenReturn(buildVpcsWebJson());

        Mockito.when(subnetManagerRestClient.getSubnet(UnitTestConfig.projectId, UnitTestConfig.subnetId))
                .thenReturn(buildSubnetWebJson());

        Mockito.when(subnetManagerRestClient.getSubnetBulk(anyString(), anyList()))
                .thenReturn(buildSubnetsWebJson());

        Mockito.when(ipManagerRestClient.allocateIpAddress(any(IpAddrRequest.class)))
                .thenReturn(buildIpv4AddrRequest(UnitTestConfig.ip1));

        Mockito.when(ipManagerRestClient.allocateIpAddressBulk(anyList()))
                .thenReturn(buildIpAddrRequestBulk());

        Mockito.when(macManagerRestClient.allocateMacAddress(any(MacState.class)))
                .thenReturn(buildMacStateJson(UnitTestConfig.portId1, UnitTestConfig.mac1));

        Mockito.when(macManagerRestClient.allocateMacAddressBulk(anyList()))
                .thenReturn(buildMacStateBulkJson(UnitTestConfig.portId1));

        Mockito.when(macManagerRestClient.allocateMacAddressBulk(anyList()))
                .thenReturn(buildMacStateBulkJson(UnitTestConfig.portId1));

        Mockito.when(routeManagerRestClient.getSubnetRoute(UnitTestConfig.subnetId))
                .thenReturn(buildRoutesWebJson());
      
        Mockito.when(securityGroupManagerRestClient.getSecurityGroup(UnitTestConfig.projectId, UnitTestConfig.securityGroupId1))
                .thenReturn(buildSecurityGroupWebJson(UnitTestConfig.securityGroupId1));

        Mockito.when(securityGroupManagerRestClient.getSecurityGroup(UnitTestConfig.projectId, UnitTestConfig.securityGroupId2))
                .thenReturn(buildSecurityGroupWebJson(UnitTestConfig.securityGroupId2));

        Mockito.when(securityGroupManagerRestClient.getSecurityGroupBulk(anyString(), anyList()))
                .thenReturn(buildSecurityGroupsJson(UnitTestConfig.securityGroupId1));

        Mockito.when(securityGroupManagerRestClient.getDefaultSecurityGroup(UnitTestConfig.projectId, UnitTestConfig.tenantId))
                .thenReturn(buildDefaultSecurityGroupWebJson());

        Mockito.when(nodeManagerRestClient.getNodeInfo(UnitTestConfig.nodeId1))
                .thenReturn(buildNodeInfoJson(UnitTestConfig.nodeId1, UnitTestConfig.ip1));

        Mockito.when(nodeManagerRestClient.getNodeInfo(UnitTestConfig.nodeId2))
                .thenReturn(buildNodeInfoJson(UnitTestConfig.nodeId2, UnitTestConfig.ip2));

        List<NodeInfo> nodeInfos = new ArrayList<>();
        NodeInfo nodeInfo = new NodeInfo(UnitTestConfig.nodeId,
                UnitTestConfig.nodeName,
                UnitTestConfig.nodeLocalIp,
                UnitTestConfig.nodeMacAddress,
                UnitTestConfig.nodeVeth,
                UnitTestConfig.nodeGRPCServerPort,
                UnitTestConfig.nodeUnicastTopic,
                UnitTestConfig.nodeMulticastTopic,
                UnitTestConfig.nodeGroupTopic
        );
        nodeInfos.add(nodeInfo);
        Mockito.when(nodeManagerRestClient.getNodeInfoByNodeName(anyString())).thenReturn(nodeInfos);

        Mockito.when(portRepository.findPortEntity(UnitTestConfig.portId1))
                .thenReturn(buildPortWebJson(UnitTestConfig.portId1).getPortEntity());

        Mockito.when(portRepository.findPortEntity(UnitTestConfig.portId2))
                .thenReturn(buildPortWebJson(UnitTestConfig.portId2).getPortEntity());

        Map<String, PortEntity> portStates = new HashMap<>();
        portStates.put(UnitTestConfig.portId1, buildPortWebJson(UnitTestConfig.portId1).getPortEntity());

        Mockito.when(portRepository.findAllPortEntities(anyMap()))
                .thenReturn(portStates);

        Mockito.when(portRepository.getPortNeighbors(UnitTestConfig.vpcId))
                .thenReturn(buildPortNeighbors(UnitTestConfig.portId1));

        Mockito.when(portRepository.getNeighbors(UnitTestConfig.vpcId))
                .thenReturn(buildNeighbors());

        Mockito.when(elasticIpManagerRestClient.updateElasticIp(any()))
                .thenReturn(buildElasticIp());

        Mockito.when(routerManagerRestClient.getRouterSubnets(
                UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.subnetId))
                .thenReturn(buildRouterSubnets());

        Mockito.when(portRepository.getSubnetPortCount(UnitTestConfig.subnetId))
                .thenReturn(buildSubnetPorts());
    }
}
