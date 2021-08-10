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
package com.futurewei.alcor.route;

import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.route.config.UnitTestConfig;
import com.futurewei.alcor.route.service.*;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
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
public class NeutronRouterTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NeutronRouterToSubnetService routerToSubnetService;

    @MockBean
    private RouterDatabaseService routerDatabaseService;

    @MockBean
    private RouterExtraAttributeDatabaseService routerExtraAttributeDatabaseService;

    @MockBean
    private RouteTableDatabaseService routeTableDatabaseService;

    @MockBean
    private RouterToPMService routerToPMService;

    @MockBean
    private RouterToDPMService routerToDPMService;

    @MockBean
    private RouterService routerService;

    private String getNeutronRouterByRouterIdUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId;
    private String createNeutronRoutersUri = "/project/" + UnitTestConfig.projectId + "/routers";
    private String updateNeutronRouterByRouterIdUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId;
    private String deleteNeutronRouterByRouterIdUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId;
    private String addInterfaceToNeutronRouterUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId + "/add_router_interface";
    private String removeInterfaceToNeutronRouterUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId + "/remove_router_interface";
    private String addRoutesToNeutronRouterUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId + "/add_extra_routes";
    private String removeRoutesToNeutronRouterUri = "/project/" + UnitTestConfig.projectId + "/routers/" + UnitTestConfig.routerId + "/remove_extra_routes";
    private String getConnectedSubnets = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/subnets/" + UnitTestConfig.subnetId + "/connected-subnets";

    @Test
    public void getNeutronRouterById_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);}});
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});
        this.mockMvc.perform(get(getNeutronRouterByRouterIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.router.id").value(UnitTestConfig.routerId));
    }

    @Test
    public void getNeutronRouterById_canNotFindRouter_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});
        try {
            String response = this.mockMvc.perform(get(getNeutronRouterByRouterIdUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertEquals("{\"router\":null}", response);
        }catch (Exception e) {
            System.out.println("-----json returned =" + e.getMessage());
            throw e;
        }
    }

    @Test
    public void createNeutronRouters_pass () throws Exception {
        try {
            this.mockMvc.perform(post(createNeutronRoutersUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.neutronRouterResource))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.router.id").value(UnitTestConfig.routerId));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void updateNeutronRouter_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);}});
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});

        try {
            this.mockMvc.perform(put(updateNeutronRouterByRouterIdUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.neutronRouterUpdateResource))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.router.id").value(UnitTestConfig.updateRouterId));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void updateNeutronRouter_canNotFindRouter_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});
        try {
            String response = this.mockMvc.perform(get(getNeutronRouterByRouterIdUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertEquals("{\"router\":null}", response);
        }catch (Exception e) {
            System.out.println("-----json returned =" + e.getMessage());
            throw e;
        }
    }

    @Test
    public void deleteNeutronRouter_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);setRouterExtraAttributeId(UnitTestConfig.routerExtraAttributeId);}});
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});
        try {
            this.mockMvc.perform(delete(deleteNeutronRouterByRouterIdUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.routerId));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void deleteNeutronRouter_canNotFindRouter_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(new RouterExtraAttribute(){{setId(UnitTestConfig.routerExtraAttributeId);}});
        try {
            String response = this.mockMvc.perform(delete(deleteNeutronRouterByRouterIdUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":null}", response);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void deleteNeutronRouter_canNotFindRouterExtraAttribute_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);setRouterExtraAttributeId(UnitTestConfig.routerExtraAttributeId);}});
        Mockito.when(routerExtraAttributeDatabaseService.getByRouterExtraAttributeId(anyString()))
                .thenReturn(null);
        try {
            String response = this.mockMvc.perform(delete(deleteNeutronRouterByRouterIdUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":null}", response);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void addInterfaceToNeutronRouter_onlyPassInPortId_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);setProjectId(UnitTestConfig.projectId);setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>(){{put(UnitTestConfig.routeTableId, new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity());}});
        Mockito.when(routerService.getSubnetRouteTable(anyString(), anyString()))
                .thenReturn(new RouteTable());

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_port))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.port_id").value(UnitTestConfig.portId));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addInterfaceToNeutronRouter_onlyPassInSubnetId_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);setProjectId(UnitTestConfig.projectId);setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>(){{put(UnitTestConfig.routeTableId, new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});
        Mockito.when(routerService.getSubnetRouteTable(anyString(), anyString()))
                .thenReturn(new RouteTable());

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnet))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet_id").value(UnitTestConfig.subnetId));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addInterfaceToNeutronRouter_passInBothSubnetIdAndPortId_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnetAndPort))
                    .andDo(print())
                    .andExpect(status().is(400));
        } catch (Exception e) {
            System.out.println("-----json returned =" + e.getMessage());
            throw e;
        }

    }

    @Test
    public void addInterfaceToNeutronRouter_onlyPassInPortId_SubnetNotBindUniquePortId_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});add(new SubnetEntity());}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity());}});

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_port))
                    .andDo(print())
                    .andExpect(status().is(409));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addInterfaceToNeutronRouter_onlyPassInPortId_PortIsAlreadyInUse_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity());}});

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_port))
                    .andDo(print())
                    .andExpect(status().is(409));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addInterfaceToNeutronRouter_onlyPassInPortId_CanNotFindRouter_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity());}});

        try {
            this.mockMvc.perform(put(addInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_port))
                    .andDo(print())
                    .andExpect(status().is(500));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_onlyPassInPortId_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});}});

        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});
        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_port))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.port_id").value(UnitTestConfig.portId));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_onlyPassInSubnetId_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});
        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnet))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet_id").value(UnitTestConfig.subnetId));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_passInBothSubnetIdAndPortId_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});setAttachedRouterId(UnitTestConfig.routerId);}});}});

        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnetAndPort))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet_id").value(UnitTestConfig.subnetId));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_passInBothSubnetIdAndPortId_SubnetNotBindUniquePortId_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});add(new SubnetEntity());}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.diffPortId);}});}});}});

        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnetAndPort))
                    .andDo(print())
                    .andExpect(status().is(409));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_passInBothSubnetIdAndPortId_AttachedPortsNotMatchPortId_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.diffPortId);}});}});}});

        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnetAndPort))
                    .andDo(print())
                    .andExpect(status().is(409));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeInterfaceToNeutronRouter_passInBothSubnetIdAndPortId_RouterOrSubnetAndPortNotExistOrNotVisible_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});

        try {
            this.mockMvc.perform(put(removeInterfaceToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routerInterfaceRequest_subnetAndPort))
                    .andDo(print())
                    .andExpect(status().is(404));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addRoutesToNeutronRouter_pass () throws Exception {

        Map<String, RouteTable> routeTableMap = new HashMap<>();
        routeTableMap.put(UnitTestConfig.routeTableId, new RouteTable(){{setRouteEntities(new ArrayList<>(){{add(new RouteEntry(){{setDestination(UnitTestConfig.routeId);setNexthop(UnitTestConfig.routeId);}});}});}});

        Mockito.when(routerToPMService.getSubnetIdsFromPM(anyString(), anyList()))
                .thenReturn(new ArrayList<>());
        Mockito.doNothing().when(routerToDPMService).sendInternalRouterInfoToDPM(any(InternalRouterInfo.class));
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});
        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(routeTableMap);

        try {
            this.mockMvc.perform(put(addRoutesToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routesToNeutronRouterRequest))
                    .andDo(print())
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void addRoutesToNeutronRouter_RouterOrSubnetAndPortNotExistOrNotVisible_notPass () throws Exception {

        Map<String, RouteTable> routeTableMap = new HashMap<>();
        routeTableMap.put(UnitTestConfig.routeTableId, new RouteTable(){{setRouteEntities(new ArrayList<>(){{add(new RouteEntry(){{setDestination(UnitTestConfig.routeId);setNexthop(UnitTestConfig.routeId);}});}});}});

        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});
        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(routeTableMap);

        try {
            this.mockMvc.perform(put(addRoutesToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routesToNeutronRouterRequest))
                    .andDo(print())
                    .andExpect(status().is(404));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeRoutesToNeutronRouter_pass () throws Exception {

        Map<String, RouteTable> routeTableMap = new HashMap<>();
        routeTableMap.put(UnitTestConfig.routeTableId, new RouteTable(){{setRouteEntities(new ArrayList<>(){{add(new RouteEntry(){{setDestination(UnitTestConfig.routeId);setNexthop(UnitTestConfig.routeId);}});}});}});

        Mockito.when(routerToPMService.getSubnetIdsFromPM(anyString(), anyList()))
                .thenReturn(new ArrayList<>());
        Mockito.doNothing().when(routerToDPMService).sendInternalRouterInfoToDPM(any(InternalRouterInfo.class));
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});
        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(routeTableMap);

        try {
            this.mockMvc.perform(put(removeRoutesToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routesToNeutronRouterRequest))
                    .andDo(print())
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void removeRoutesToNeutronRouter_RouterOrSubnetAndPortNotExistOrNotVisible_notPass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(null);
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});

        try {
            this.mockMvc.perform(put(removeRoutesToNeutronRouterUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.routesToNeutronRouterRequest))
                    .andDo(print())
                    .andExpect(status().is(404));
        } catch (Exception e) {
            throw e;
        }

    }

    @Test
    public void getConnectedSubnets_pass () throws Exception {
        Mockito.when(routerDatabaseService.getByRouterId(UnitTestConfig.routerId))
                .thenReturn(new Router(){{setId(UnitTestConfig.routerId);
                    setGatewayPorts(new ArrayList<>());
                    setNeutronRouteTable(new RouteTable(){{setRouteEntities(new ArrayList<>());setRouteTableType(RouteTableType.NEUTRON_ROUTER.getRouteTableType());}});}});
        Mockito.when(routerToSubnetService.getSubnetsByPortId(anyString(), anyString()))
                .thenReturn(new SubnetsWebJson(){{setSubnets(new ArrayList<SubnetEntity>(){{add(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});}});
        Mockito.when(routerToSubnetService.getSubnet(anyString(), anyString()))
                .thenReturn(new SubnetWebJson(){{setSubnet(new SubnetEntity(){{setId(UnitTestConfig.subnetId);setGatewayPortDetail(new GatewayPortDetail(){{setGatewayPortId(UnitTestConfig.portId);}});}});}});

        try {
            this.mockMvc.perform(get(getConnectedSubnets))
                    .andDo(print())
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw e;
        }

    }

}
