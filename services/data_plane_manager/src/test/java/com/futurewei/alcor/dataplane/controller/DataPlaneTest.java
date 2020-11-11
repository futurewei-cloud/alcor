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
package com.futurewei.alcor.dataplane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.TestConfig;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.web.entity.dataplane.*;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry.NeighborType;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DataPlaneTest {
    @Autowired
    private MockMvc mockMvc;

    //@MockBean
    //private DataPlaneClient dataPlaneClient;

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
        internalSubnetEntity1.setGatewayMacAddress(TestConfig.gatewayMacAddress1);
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
        internalSubnetEntity2.setGatewayMacAddress(TestConfig.gatewayMacAddress2);
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

    private NetworkConfiguration buildNetworkConfiguration() {
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
    public void createNetworkConfigurationTest() throws Exception {
        NetworkConfiguration networkConfiguration = buildNetworkConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(networkConfiguration);

        this.mockMvc.perform(MockMvcRequestBuilders.post(TestConfig.url)
                .content(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }
}
