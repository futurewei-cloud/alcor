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
package com.futurewei.alcor.subnet;

import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.subnet.config.UnitTestConfig;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
import com.futurewei.alcor.subnet.service.SubnetToPortManagerService;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class SubnetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubnetDatabaseService subnetDatabaseService;

    @MockBean
    private SubnetService subnetService;

    @MockBean
    private SubnetToPortManagerService subnetToPortManagerService;

    private String getByIdUri = "/project/" + UnitTestConfig.projectId + "/subnets/" + UnitTestConfig.subnetId;
    private String createUri = "/project/" + UnitTestConfig.projectId + "/subnets";
    private String getByProjectIdAndVpcIdUri = "/project/" + UnitTestConfig.projectId + "/subnets";
    private String deleteUri = "/project/" + UnitTestConfig.projectId + "/subnets/" + UnitTestConfig.subnetId;
    private String putUri = "/project/" + UnitTestConfig.projectId + "/subnets/" + UnitTestConfig.subnetId;

    @Test
    public void subnetGetById_canFindSubnet_pass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void subnetGetById_canNotFindSubnet_notPass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"subnet\":null}", response);
    }

    @Test
    public void createSubnetState_create_pass () throws Exception {
        SubnetEntity subnetEntity = new SubnetEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr);
        VpcEntity vpcState = new VpcEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.cidr, new ArrayList<RouteEntity>(){{add(new RouteEntity());}});

        VpcWebJson vpcWebJson = new VpcWebJson(vpcState);
        RouteWebJson routeWebJson = new RouteWebJson();
        MacStateJson macResponse = new MacStateJson();
        IpAddrRequest ipAddrRequest = new IpAddrRequest();

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetEntity);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        Mockito.when(subnetService.createRouteRules(eq(UnitTestConfig.subnetId), any(SubnetEntity.class)))
                .thenReturn(routeWebJson);
        Mockito.when(subnetService.allocateMacAddressForGatewayPort(anyString(), anyString(), anyString()))
                .thenReturn(macResponse);
        Mockito.when(subnetService.allocateIpAddressForGatewayPort(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(ipAddrRequest);
        Mockito.when(subnetService.cidrToFirstIpAndLastIp(UnitTestConfig.cidr))
                .thenReturn(new String[2]);
        Mockito.when(subnetToPortManagerService.createGatewayPort(anyString(), any(PortEntity.class)))
                .thenReturn(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));
        Mockito.when(subnetService.constructPortEntity(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PortEntity());

        this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.resource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void createSubnetState_canNotFindVpcState_notPass () throws Exception {
        SubnetEntity subnetEntity = new SubnetEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr);
        VpcEntity vpcState = new VpcEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.cidr, new ArrayList<RouteEntity>(){{add(new RouteEntity());}});
        MacState macState = new MacState();
        macState.setMacAddress(UnitTestConfig.macAddress);
        RouteWebJson routeWebJson = new RouteWebJson();
        RouteEntity routeEntity = new RouteEntity();
        routeWebJson.setRoute(routeEntity);
        MacStateJson macResponse = new MacStateJson(macState);
        IpAddrRequest ipAddrRequest = new IpAddrRequest();

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetEntity);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenThrow(new FallbackException("fallback request"));
        Mockito.when(subnetService.createRouteRules(eq(UnitTestConfig.subnetId), any(SubnetEntity.class)))
                .thenReturn(routeWebJson);
        Mockito.when(subnetService.allocateMacAddressForGatewayPort(anyString(), anyString(), anyString()))
                .thenReturn(macResponse);
        Mockito.when(subnetService.allocateIpAddressForGatewayPort(UnitTestConfig.subnetId, UnitTestConfig.cidr, UnitTestConfig.vpcId, UnitTestConfig.gatewayIp,true))
                .thenReturn(ipAddrRequest);
        try {
            this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.resource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
        }catch (Exception ex) {
            //System.out.println(ex.getMessage());
            assertEquals(UnitTestConfig.createException, ex.getMessage());
        }

    }

    @Test
    public void createSubnetState_canNotFindIP_notPass () throws Exception {
        SubnetEntity subnetEntity = new SubnetEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr);
        VpcEntity vpcState = new VpcEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.cidr, new ArrayList<RouteEntity>(){{add(new RouteEntity());}});

        RouteWebJson routeWebJson = new RouteWebJson();
        RouteEntity routeEntity = new RouteEntity();
        routeWebJson.setRoute(routeEntity);
        VpcWebJson vpcWebJson = new VpcWebJson(vpcState);
        MacStateJson macResponse = new MacStateJson();
        MacState macState = new MacState();
        macResponse.setMacState(macState);

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetEntity);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        Mockito.when(subnetService.createRouteRules(eq(UnitTestConfig.subnetId), any(SubnetEntity.class)))
                .thenReturn(routeWebJson);
        Mockito.when(subnetService.allocateMacAddressForGatewayPort(anyString(), anyString(), anyString()))
                .thenReturn(macResponse);
        Mockito.when(subnetService.cidrToFirstIpAndLastIp(UnitTestConfig.cidr))
                .thenReturn(new String[2]);
        Mockito.when(subnetService.allocateIpAddressForGatewayPort(UnitTestConfig.subnetId, UnitTestConfig.cidr, UnitTestConfig.vpcId, UnitTestConfig.gatewayIp,true))
                .thenThrow(new FallbackException("fallback request"));
        Mockito.when(subnetToPortManagerService.createGatewayPort(anyString(), any(PortEntity.class)))
                .thenReturn(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));
        Mockito.when(subnetService.constructPortEntity(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PortEntity());
        try {
            this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.resource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
        }catch (Exception ex) {
            //System.out.println(ex.getMessage());
            assertEquals(UnitTestConfig.createFallbackException, ex.getMessage());
        }
    }

    @Test
    public void createSubnetState_invalidCidr_notPass () throws Exception {
        SubnetEntity subnetEntity = new SubnetEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.invalidCidr);
        VpcEntity vpcState = new VpcEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.invalidCidr, new ArrayList<RouteEntity>(){{add(new RouteEntity());}});

        RouteWebJson routeWebJson = new RouteWebJson();
        RouteEntity routeEntity = new RouteEntity();
        routeWebJson.setRoute(routeEntity);
        VpcWebJson vpcWebJson = new VpcWebJson(vpcState);
        MacStateJson macResponse = new MacStateJson();
        MacState macState = new MacState();
        macResponse.setMacState(macState);
        IpAddrRequest ipAddrRequest = new IpAddrRequest();

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetEntity);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        Mockito.when(subnetService.createRouteRules(eq(UnitTestConfig.subnetId), any(SubnetEntity.class)))
                .thenReturn(routeWebJson);
        Mockito.when(subnetService.allocateMacAddressForGatewayPort(anyString(), anyString(), anyString()))
                .thenReturn(macResponse);
        Mockito.when(subnetService.allocateIpAddressForGatewayPort(UnitTestConfig.subnetId, UnitTestConfig.invalidCidr, UnitTestConfig.vpcId, UnitTestConfig.gatewayIp,false))
                .thenReturn(ipAddrRequest);
        Mockito.when(subnetService.cidrToFirstIpAndLastIp(UnitTestConfig.invalidCidr))
                .thenReturn(new String[2]);
        Mockito.when(subnetToPortManagerService.createGatewayPort(anyString(), any(PortEntity.class)))
                .thenReturn(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));
        Mockito.when(subnetService.constructPortEntity(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PortEntity());
        try {
            this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.invalidCidrResource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
        }catch (Exception ex) {
            //System.out.println(ex.getMessage());
            assertEquals(UnitTestConfig.createFallbackException, ex.getMessage());
        }
    }

    @Test
    public void updateSubnetState_noUpdate_pass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId)).
                thenReturn(new SubnetEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.name, UnitTestConfig.cidr){{setGatewayPortDetail(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));}});
        Mockito.when(subnetToPortManagerService.createGatewayPort(anyString(), any(PortEntity.class)))
                .thenReturn(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));
        this.mockMvc.perform(put(putUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.resource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void updateSubnetState_update_pass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.name, UnitTestConfig.cidr){{setGatewayPortDetail(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));}})
                .thenReturn(new SubnetEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.updateName, UnitTestConfig.cidr){{setGatewayPortDetail(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));}});
        Mockito.when(subnetToPortManagerService.createGatewayPort(anyString(), any(PortEntity.class)))
                .thenReturn(new GatewayPortDetail(UnitTestConfig.macAddress, UnitTestConfig.gatewayPortId));
        this.mockMvc.perform(put(putUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.updateResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void updateSubnetState_canNotFindSubnet_notPass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenThrow(new ResourceNotFoundException("Subnet not found : " + UnitTestConfig.subnetId));
        try {
            this.mockMvc.perform(put(putUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.resource))
                    .andDo(print())
                    .andExpect(status().isOk());
        }catch (Exception ex) {
            //System.out.println(ex.getMessage());
            assertEquals(UnitTestConfig.exception, ex.getMessage());
        }

    }

    @Test
    public void getSubnetStatesByProjectIdAndVpcId_getMap_pass () throws Exception {
        Map<String, SubnetEntity> subnetStates = new HashMap<>();
        SubnetEntity subnetEntity = new SubnetEntity( UnitTestConfig.projectId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.name,UnitTestConfig.cidr);
        subnetStates.put("SubnetState", subnetEntity);
        Mockito.when(subnetDatabaseService.getAllSubnets()).thenReturn(subnetStates);
        this.mockMvc.perform(get(getByProjectIdAndVpcIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getSubnetStatesByProjectIdAndVpcId_getEmptyMap_pass () throws Exception {
        Map<String, SubnetEntity> subnetStates = new HashMap<>();
        Mockito.when(subnetDatabaseService.getAllSubnets()).thenReturn(subnetStates);
        this.mockMvc.perform(get(getByProjectIdAndVpcIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteSubnetState_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetEntity( UnitTestConfig.projectId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.name,UnitTestConfig.cidr));
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void deleteSubnetState_deleteWhenIdNotExist_pass () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"id\":null}", response);
    }

    @Before
    public void init() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }
}
