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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.portmanager.repo.IPortRepository;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.web.entity.ip.IpAddrUpdateRequest;
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
    private IPortRepository portRepository;

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

        Mockito.when(ipManagerRestClient.updateIpAddress(any(IpAddrUpdateRequest.class)))
                .thenReturn(buildIpAddrUpdateRequest(UnitTestConfig.ip1));

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
                UnitTestConfig.nodeGRPCServerPort
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

        Mockito.when(portRepository.getNeighbors(UnitTestConfig.vpcId))
                .thenReturn(buildNeighbors());

        Mockito.when(elasticIpManagerRestClient.updateElasticIp(any()))
                .thenReturn(buildElasticIp());

        Mockito.when(routerManagerRestClient.getRouterSubnets(
                UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.subnetId))
                .thenReturn(buildRouterSubnets());
    }
}
