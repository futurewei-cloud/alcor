/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.dataplane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.client.grpc.DataPlaneClientImpl;
import com.futurewei.alcor.dataplane.config.TestConfig;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry.NeighborType;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortHostInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterConfiguration;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.InternalRoutingRule;
import com.futurewei.alcor.web.entity.route.InternalSubnetRoutingTable;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.futurewei.alcor.common.enumClass.MessageType.FULL;
import static com.futurewei.alcor.common.enumClass.OperationType.CREATE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
//@ComponentScan(value = "com.futurewei.alcor.common.test.config")
public class DpmTest {
    private static final String FORMAT_REVISION_NUMBER = "1";
    private static final String ROUTER_REQUEST_ID = "1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PulsarClient pulsarClient;

    @MockBean
    private DataPlaneClientImpl grpcDataPlaneClient;

    private List<VpcEntity> buildVpcEntities() {
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setProjectId(TestConfig.projectId);
        vpcEntity.setId(TestConfig.vpcId);
        vpcEntity.setName(TestConfig.vpcName);
        vpcEntity.setCidr(TestConfig.vpcCidr);
        vpcEntity.setTenantId(TestConfig.tenantId);

        List<VpcEntity> vpcEntities = new ArrayList<>();
        vpcEntities.add(vpcEntity);

        return vpcEntities;
    }

    private List<InternalSubnetEntity> buildSubnetEntities() {
        InternalSubnetEntity internalSubnetEntity1 = new InternalSubnetEntity();
        internalSubnetEntity1.setId(TestConfig.subnetId1);
        internalSubnetEntity1.setProjectId(TestConfig.projectId);
        internalSubnetEntity1.setVpcId(TestConfig.vpcId);
        internalSubnetEntity1.setName(TestConfig.subnetName1);
        internalSubnetEntity1.setCidr(TestConfig.subnetCidr1);
        internalSubnetEntity1.setTunnelId(TestConfig.tunnelId);
        internalSubnetEntity1.setGatewayIp(TestConfig.gatewayIp1);
        internalSubnetEntity1.setGatewayPortDetail(new GatewayPortDetail(TestConfig.gatewayMacAddress1, null));
        internalSubnetEntity1.setDhcpEnable(true);
        internalSubnetEntity1.setAvailabilityZone(TestConfig.availabilityZone);
        internalSubnetEntity1.setPrimaryDns(TestConfig.primaryDns);
        internalSubnetEntity1.setSecondaryDns(TestConfig.secondaryDns);

        InternalSubnetEntity internalSubnetEntity2 = new InternalSubnetEntity();
        internalSubnetEntity2.setId(TestConfig.subnetId2);
        internalSubnetEntity2.setProjectId(TestConfig.projectId);
        internalSubnetEntity2.setVpcId(TestConfig.vpcId);
        internalSubnetEntity2.setName(TestConfig.subnetName2);
        internalSubnetEntity2.setCidr(TestConfig.subnetCidr2);
        internalSubnetEntity2.setTunnelId(TestConfig.tunnelId);
        internalSubnetEntity2.setGatewayIp(TestConfig.gatewayIp2);
        internalSubnetEntity2.setGatewayPortDetail(new GatewayPortDetail(TestConfig.gatewayMacAddress2, null));
        internalSubnetEntity2.setDhcpEnable(true);
        internalSubnetEntity2.setAvailabilityZone(TestConfig.availabilityZone);
        internalSubnetEntity2.setPrimaryDns(TestConfig.primaryDns);
        internalSubnetEntity2.setSecondaryDns(TestConfig.secondaryDns);

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        subnetEntities.add(internalSubnetEntity1);
        subnetEntities.add(internalSubnetEntity2);

        return subnetEntities;
    }

    private List<InternalPortEntity> buildPortEntities() {
        InternalPortEntity internalPortEntity = new InternalPortEntity();
        internalPortEntity.setId(TestConfig.portId1);
        internalPortEntity.setProjectId(TestConfig.projectId);
        internalPortEntity.setVpcId(TestConfig.vpcId);
        internalPortEntity.setName(TestConfig.portName1);
        internalPortEntity.setMacAddress(TestConfig.mac1);
        internalPortEntity.setAdminStateUp(true);
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(new PortEntity.FixedIp(TestConfig.subnetId1, TestConfig.ip11));
        internalPortEntity.setFixedIps(fixedIps);
        //internalPortEntity.setAllowedAddressPairs();
        //List<String> securityGroupIds = new ArrayList<>();
        //securityGroupIds.add(UnitTestConfig.securityGroupId1);
        //securityGroupIds.add(UnitTestConfig.securityGroupId2);
        //internalPortEntity.setSecurityGroups(securityGroupIds);
        internalPortEntity.setBindingHostIP(TestConfig.hostIp1);

        List<InternalPortEntity> portEntities = new ArrayList<>();
        portEntities.add(internalPortEntity);

        return portEntities;
    }

    private Map<String, NeighborInfo> buildNeighborInfos() {
        Map<String, NeighborInfo> neighborInfos = new HashMap<>();
        NeighborInfo neighborInfo11 = new NeighborInfo();
        neighborInfo11.setHostIp(TestConfig.hostIp1);
        neighborInfo11.setPortIp(TestConfig.ip11);
        neighborInfo11.setVpcId(TestConfig.vpcId);
        neighborInfo11.setSubnetId(TestConfig.subnetId1);
        neighborInfo11.setPortId(TestConfig.portId1);
        neighborInfo11.setPortMac(TestConfig.mac1);

        NeighborInfo neighborInfo12 = new NeighborInfo();
        neighborInfo12.setHostIp(TestConfig.hostIp1);
        neighborInfo12.setPortIp(TestConfig.ip12);
        neighborInfo12.setVpcId(TestConfig.vpcId);
        neighborInfo12.setSubnetId(TestConfig.subnetId1);
        neighborInfo12.setPortId(TestConfig.portId2);
        neighborInfo12.setPortMac(TestConfig.mac1);

        NeighborInfo neighborInfo21 = new NeighborInfo();
        neighborInfo21.setHostIp(TestConfig.hostIp2);
        neighborInfo21.setPortIp(TestConfig.ip21);
        neighborInfo21.setVpcId(TestConfig.vpcId);
        neighborInfo21.setSubnetId(TestConfig.subnetId2);
        neighborInfo21.setPortId(TestConfig.portId3);
        neighborInfo21.setPortMac(TestConfig.mac1);

        NeighborInfo neighborInfo22 = new NeighborInfo();
        neighborInfo22.setHostIp(TestConfig.hostIp2);
        neighborInfo22.setPortIp(TestConfig.ip22);
        neighborInfo22.setVpcId(TestConfig.vpcId);
        neighborInfo22.setSubnetId(TestConfig.subnetId2);
        neighborInfo22.setPortId(TestConfig.portId4);
        neighborInfo22.setPortMac(TestConfig.mac1);

        neighborInfos.put(TestConfig.ip11, neighborInfo11);
        neighborInfos.put(TestConfig.ip12, neighborInfo12);
        neighborInfos.put(TestConfig.ip21, neighborInfo21);
        neighborInfos.put(TestConfig.ip22, neighborInfo22);

        return neighborInfos;
    }

    private Map<String, List<NeighborEntry>> buildNeighborTable() {
        Map<String, List<NeighborEntry>> neighborTable = new HashMap<>();
        List<NeighborEntry> neighborEntries = new ArrayList<>();

        NeighborEntry neighborEntry1 = new NeighborEntry();
        neighborEntry1.setLocalIp(TestConfig.ip11);
        neighborEntry1.setNeighborIp(TestConfig.ip12);
        neighborEntry1.setNeighborType(NeighborType.L2);

        NeighborEntry neighborEntry2 = new NeighborEntry();
        neighborEntry2.setLocalIp(TestConfig.ip11);
        neighborEntry2.setNeighborIp(TestConfig.ip21);
        neighborEntry2.setNeighborType(NeighborType.L3);

        NeighborEntry neighborEntry3 = new NeighborEntry();
        neighborEntry3.setLocalIp(TestConfig.ip11);
        neighborEntry3.setNeighborIp(TestConfig.ip22);
        neighborEntry3.setNeighborType(NeighborType.L3);

        neighborEntries.add(neighborEntry1);
        neighborEntries.add(neighborEntry2);
        neighborEntries.add(neighborEntry3);
        neighborTable.put(TestConfig.ip11, neighborEntries);

        return neighborTable;
    }

    private List<SecurityGroup> buildSecurityGroups() {
        List<SecurityGroup> securityGroups = new ArrayList<>();
        return securityGroups;
    }

    private NetworkConfiguration buildPortConfiguration() {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setRsType(ResourceType.PORT);
        networkConfiguration.setOpType(OperationType.CREATE);
        networkConfiguration.setVpcs(buildVpcEntities());
        networkConfiguration.setSubnets(buildSubnetEntities());
        networkConfiguration.setPortEntities(buildPortEntities());
        networkConfiguration.setNeighborInfos(buildNeighborInfos());
        networkConfiguration.setNeighborTable(buildNeighborTable());
        networkConfiguration.setSecurityGroups(buildSecurityGroups());

        return networkConfiguration;
    }

    @Test
    public void createPortConfigurationTest() throws Exception {
        NetworkConfiguration networkConfiguration = buildPortConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(networkConfiguration);

        this.mockMvc.perform(MockMvcRequestBuilders.post(TestConfig.url)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

        Consumer<MulticastGoalStateByte> multicastConsumer = pulsarClient.newConsumer(JSONSchema.of(MulticastGoalStateByte.class))
                .topic("multicast-topic2")
                .subscriptionName("my-subscription")
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();

        Message<MulticastGoalStateByte> msg = multicastConsumer.receive();
        try {
            multicastConsumer.acknowledge(msg);
        } catch (Exception e) {
            System.err.printf("Unable to consume message: %s", e.getMessage());
            multicastConsumer.negativeAcknowledge(msg);
        }
        Assert.assertNotNull(msg.getValue().getGoalStateByte());
    }

    private Map<String, InternalSubnetPorts> buildSubnetPorts() {
        Map<String, InternalSubnetPorts> subnetPortsMap = new HashMap<>();

        InternalSubnetPorts subnetPorts1 = new InternalSubnetPorts();
        subnetPorts1.setSubnetId(TestConfig.subnetId1);
        subnetPorts1.setGatewayPortId(TestConfig.portId1);
        subnetPorts1.setGatewayPortIp(TestConfig.ip11);
        subnetPorts1.setGatewayPortMac(TestConfig.mac1);
        List<PortHostInfo> portHostInfos1 = new ArrayList<>();
        PortHostInfo portHostInfo1 = new PortHostInfo();
        portHostInfo1.setHostId(TestConfig.hostIp1);
        portHostInfo1.setHostIp(TestConfig.hostIp1);
        portHostInfo1.setPortId(TestConfig.portId1);
        portHostInfo1.setPortIp(TestConfig.ip11);
        portHostInfo1.setPortMac(TestConfig.mac1);
        portHostInfos1.add(portHostInfo1);
        subnetPorts1.setPorts(portHostInfos1);

        InternalSubnetPorts subnetPorts2 = new InternalSubnetPorts();
        subnetPorts2.setSubnetId(TestConfig.subnetId2);
        subnetPorts2.setGatewayPortId(TestConfig.portId2);
        subnetPorts2.setGatewayPortIp(TestConfig.ip21);
        subnetPorts2.setGatewayPortMac(TestConfig.mac2);
        List<PortHostInfo> portHostInfos2 = new ArrayList<>();
        PortHostInfo portHostInfo2 = new PortHostInfo();
        portHostInfo2.setHostId(TestConfig.hostIp2);
        portHostInfo2.setHostIp(TestConfig.hostIp2);
        portHostInfo2.setPortId(TestConfig.portId2);
        portHostInfo2.setPortIp(TestConfig.ip21);
        portHostInfo2.setPortMac(TestConfig.mac2);
        portHostInfos2.add(portHostInfo2);
        subnetPorts2.setPorts(portHostInfos2);

        subnetPortsMap.put(TestConfig.subnetId1, subnetPorts1);
        subnetPortsMap.put(TestConfig.subnetId2, subnetPorts2);

        return subnetPortsMap;
    }

    private List<InternalRouterInfo> buildInternalRouterInfo() {
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();

        InternalRouterInfo routerInfo1 = new InternalRouterInfo();
        routerInfo1.setOperationType(CREATE);

        InternalRouterConfiguration routerConfig = new InternalRouterConfiguration();
        routerConfig.setFormatVersion(FORMAT_REVISION_NUMBER);
        routerConfig.setRevisionNumber(FORMAT_REVISION_NUMBER);
        routerConfig.setHostDvrMac(TestConfig.hostDrvMac1);
        routerConfig.setId(TestConfig.routerId1);
        routerConfig.setMessageType(FULL);
        routerConfig.setRequestId(ROUTER_REQUEST_ID);

        List<InternalSubnetRoutingTable> subnetRoutingTables = new ArrayList<>();
        InternalSubnetRoutingTable subnetRoutingTable1 = new InternalSubnetRoutingTable();
        subnetRoutingTable1.setSubnetId(TestConfig.subnetId1);
        List<InternalRoutingRule> routingRules = new ArrayList<>();
        InternalRoutingRule routingRule = new InternalRoutingRule();
        routingRule.setDestination(TestConfig.destination);
        routingRule.setId(TestConfig.routingRuleId1);
        routingRule.setName("routingRule1");
        routingRule.setNextHopIp(TestConfig.nextHop);
        routingRule.setOperationType(CREATE);
        routingRule.setPriority(100);
        routingRule.setRoutingRuleExtraInfo(null);
        routingRules.add(routingRule);
        subnetRoutingTable1.setRoutingRules(routingRules);
        subnetRoutingTables.add(subnetRoutingTable1);
        routerConfig.setSubnetRoutingTables(subnetRoutingTables);

        routerInfo1.setRouterConfiguration(routerConfig);
        internalRouterInfos.add(routerInfo1);

        return internalRouterInfos;
    }

    private NetworkConfiguration buildRouterConfiguration() {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setRsType(ResourceType.ROUTER);
        networkConfiguration.setOpType(OperationType.CREATE);
        networkConfiguration.setInternalRouterInfos(buildInternalRouterInfo());
        //networkConfiguration.setInternalSubnetPorts(buildSubnetPorts());

        return networkConfiguration;
    }

    @Test
    public void createRouterConfigurationTest() throws Exception {
        createPortConfigurationTest();

        NetworkConfiguration networkConfiguration = buildRouterConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(networkConfiguration);

        this.mockMvc.perform(MockMvcRequestBuilders.post(TestConfig.url)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    private NetworkConfiguration buildNeighborConfiguration() {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setRsType(ResourceType.NEIGHBOR);
        networkConfiguration.setOpType(OperationType.CREATE);

        networkConfiguration.setNeighborInfos(buildNeighborInfos());
        networkConfiguration.setNeighborTable(buildNeighborTable());

        return networkConfiguration;
    }

    @Test
    public void createNeighborConfigurationTest() throws Exception {
        NetworkConfiguration networkConfiguration = buildNeighborConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(networkConfiguration);

        this.mockMvc.perform(MockMvcRequestBuilders.post(TestConfig.url)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }
}
