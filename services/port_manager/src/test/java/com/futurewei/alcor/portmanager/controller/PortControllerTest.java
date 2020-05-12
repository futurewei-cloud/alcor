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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.service.PortService;
import com.futurewei.alcor.web.entity.*;
import com.futurewei.alcor.web.rest.IpAddressRest;
import com.futurewei.alcor.web.rest.MacAddressRest;
import com.futurewei.alcor.web.rest.SubnetRest;
import com.futurewei.alcor.web.rest.VpcRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class PortControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortService portService;

    private String createPortUrl = "/project/" + UnitTestConfig.projectId + "/ports";
    private String updatePortUrl = createPortUrl + "/" + UnitTestConfig.portId;
    private String deletePortUrl = updatePortUrl;
    private String getPortUrl = updatePortUrl;
    private String listPortUrl = createPortUrl;

    private PortStateJson newPortStateJson() {
        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(new PortState.FixedIp(UnitTestConfig.subnetId, UnitTestConfig.ip1));

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroup);

        List<PortState.AllowAddressPair> allowedAddressPairs = new ArrayList<>();
        allowedAddressPairs.add(new PortState.AllowAddressPair(UnitTestConfig.ip2, UnitTestConfig.mac1));

        PortState portState = new PortState();
        portState.setId(UnitTestConfig.portId);
        portState.setVpcId(UnitTestConfig.vpcId);
        portState.setProjectId(UnitTestConfig.projectId);
        portState.setTenantId(UnitTestConfig.tenantId);
        portState.setFixedIps(fixedIps);
        portState.setMacAddress(UnitTestConfig.mac1);
        portState.setSecurityGroups(securityGroups);
        portState.setAllowedAddressPairs(allowedAddressPairs);

        return new PortStateJson(portState);
    }

    private IpAddrRequest newIpAddrRequest() {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipVersion);
        ipAddrRequest.setIp(UnitTestConfig.ip1);
        ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());

        return ipAddrRequest;
    }

    private String newPortStateJsonStr() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(newPortStateJson());
    }

    @Test
    public void getPortState() throws Exception {
        Mockito.when(portService.getPortState(UnitTestConfig.projectId, UnitTestConfig.portId))
                .thenReturn(newPortStateJson());

        this.mockMvc.perform(get(getPortUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.port_state.id")
                        .value(UnitTestConfig.portId)
                );
    }

    private VpcStateJson newVpcStateJson() {
        VpcState vpcState = new VpcState();
        vpcState.setId(UnitTestConfig.vpcId);
        vpcState.setCidr(UnitTestConfig.vpcCidr);

        return new VpcStateJson(vpcState);
    }

    private SubnetStateJson newSubnetStateJson() {
        SubnetState subnetState = new SubnetState();
        subnetState.setId(UnitTestConfig.subnetId);
        subnetState.setVpcId(UnitTestConfig.vpcId);
        subnetState.setIpV4RangeId(UnitTestConfig.rangeId);

        return new SubnetStateJson(subnetState);
    }

    private MacStateJson newMacStateJson() {
        MacState macState = new MacState();
        macState.setProjectId(UnitTestConfig.projectId);
        macState.setVpcId(UnitTestConfig.vpcId);
        macState.setPortId(UnitTestConfig.portId);
        macState.setMacAddress(UnitTestConfig.mac1);

        return new MacStateJson(macState);
    }

    @Test
    public void createPortTest() throws Exception {
        Mockito.when(portService.createPortState(UnitTestConfig.projectId, newPortStateJson()))
                .thenReturn(newPortStateJson());

        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portStateStr))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void updatePortTest() throws Exception {
        Mockito.when(portService.updatePortState(UnitTestConfig.projectId, UnitTestConfig.portId, newPortStateJson()))
                .thenReturn(newPortStateJson());

        this.mockMvc.perform(put(updatePortUrl)
                .content(UnitTestConfig.portStateStr)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void listPortTest() throws Exception {
        List<PortStateJson> portStateJsons = new ArrayList<>();
        portStateJsons.add(newPortStateJson());

        Mockito.when(portService.listPortState(UnitTestConfig.projectId))
                .thenReturn(portStateJsons);

        this.mockMvc.perform(get(listPortUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deletePortTest() throws Exception {
        this.mockMvc.perform(delete(deletePortUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
