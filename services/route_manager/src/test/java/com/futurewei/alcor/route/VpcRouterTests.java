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
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
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
public class VpcRouterTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouterDatabaseService routerDatabaseService;

    @MockBean
    private VpcRouterToVpcService vpcRouterToVpcService;

    @MockBean
    private RouteTableDatabaseService routeTableDatabaseService;

    @MockBean
    private VpcRouterToSubnetService vpcRouterToSubnetService;

    @MockBean
    private RouteEntryDatabaseService routeEntryDatabaseService;

    @MockBean
    private NeutronRouterService neutronRouterService;

    @MockBean
    private RouterToDPMService routerToDPMService;

    private String vpcRouterUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/router";
    private String vpcRouteTableUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/vpcroutetable";
    private String getVpcRouteTablesUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/routetables";
    private String RouteTablesUri = "/project/" + UnitTestConfig.projectId + "/routetables/" + UnitTestConfig.routeTableId;
    private String subnetRouteTableUri = "/project/" + UnitTestConfig.projectId + "/subnets/" + UnitTestConfig.subnetId + "/routetable";

    @Test
    public void getVpcRouter_alreadyHaveVpcRouter_pass () throws Exception {
        Router router = new Router();
        router.setId(UnitTestConfig.routerId);

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router);}});
        this.mockMvc.perform(get(vpcRouterUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.router.id").value(UnitTestConfig.routerId));
    }

    @Test
    public void getVpcRouter_notHaveVpcRouter_pass () throws Exception {
        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcWebJson.setNetwork(vpcEntity);

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>());
        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        this.mockMvc.perform(get(vpcRouterUri))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getVpcRouter_ExistMultipleVpcRouter_notPass () throws Exception {
        Router router1 = new Router();
        router1.setId(UnitTestConfig.routerId);
        Router router2 = new Router();
        router2.setId(UnitTestConfig.routerId);

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router1);put(UnitTestConfig.routerId, router2);}});

        try {
            this.mockMvc.perform(get(vpcRouterUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.router.id").value(UnitTestConfig.routerId));
        }catch (Exception e) {
            assertEquals("exist multiple vpc router searched by vpc id", e.getMessage());
            System.out.println("-----json returned =" + e.getMessage());
        }
    }

    @Test
    public void deleteVpcRouter_pass () throws Exception {
        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcWebJson.setNetwork(vpcEntity);

        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.VPC.getRouteTableType());}});}});

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router);}});
        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        this.mockMvc.perform(delete(vpcRouterUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.routerId));
    }

    @Test
    public void deleteVpcRouter_VpcRouterContainsSubnetRoutingTables_notPass () throws Exception {
        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcWebJson.setNetwork(vpcEntity);

        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.PRIVATE_SUBNET.getRouteTableType());}});}});

        SubnetsWebJson subnetsWebJson = new SubnetsWebJson();
        ArrayList<SubnetEntity> subnets = new ArrayList<>(){{add(new SubnetEntity());add(new SubnetEntity());}};
        subnetsWebJson.setSubnets(subnets);


        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router);}});
        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        Mockito.when(vpcRouterToSubnetService.getSubnetsByVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(subnetsWebJson);

        try {
            this.mockMvc.perform(delete(vpcRouterUri))
                    .andDo(print())
                    .andExpect(status().is(409));
        }catch (Exception e) {
            assertEquals("there are some subnets exist in the VPC. We cannot delete VPC router and VPC default routing table.", e.getMessage());
            System.out.println("-----json returned =" + e.getMessage());
        }
    }

    @Test
    public void getVpcRouteTable_pass () throws Exception {
        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.VPC.getRouteTableType());setId(UnitTestConfig.routeTableId);}});}});

        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcEntity.setRouter(router);
        vpcWebJson.setNetwork(vpcEntity);

        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);

        this.mockMvc.perform(get(vpcRouteTableUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable.id").value(UnitTestConfig.routeTableId));
    }

    @Test
    public void getVpcRouteTable_notpass () throws Exception {

        Router router = new Router();
        router.setId(UnitTestConfig.routerId);

        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcEntity.setRouter(router);
        vpcWebJson.setNetwork(vpcEntity);

        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);

        this.mockMvc.perform(get(vpcRouteTableUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable").doesNotExist());
    }

    @Test
    public void updateVpcRouteTable_pass () throws Exception {
        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcWebJson.setNetwork(vpcEntity);

        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcDefaultRouteTableId(UnitTestConfig.routeTableId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.VPC.getRouteTableType());setId(UnitTestConfig.routeTableId);setRouteEntities(new ArrayList<>());}});}});

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router);}});
        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        Mockito.when(routeTableDatabaseService.getByRouteTableId(UnitTestConfig.routeTableId))
                .thenReturn(new RouteTable(){{setId(UnitTestConfig.routeTableId);}});

        this.mockMvc.perform(put(vpcRouteTableUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.vpcRouteTableResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable.id").value(UnitTestConfig.routeTableId));
    }

    @Test
    public void updateVpcRouteTable_ResourceNotValid_notPass () throws Exception {
        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcWebJson.setNetwork(vpcEntity);

        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.VPC.getRouteTableType());setId(UnitTestConfig.routeTableId);setRouteEntities(new ArrayList<>());}});}});

        Mockito.when(routerDatabaseService.getAllRouters(anyMap()))
                .thenReturn(new HashMap<String, Router>(){{put(UnitTestConfig.routerId, router);}});
        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);

        try {
            this.mockMvc.perform(put(vpcRouteTableUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.vpcRouteTableExceptionResource))
                    .andDo(print())
                    .andExpect(status().isOk());
        }catch (Exception e) {
            assertEquals("Request processing failed; nested exception is com.futurewei.alcor.common.exception.ResourceNotValidException: request resource is invalid", e.getMessage());
            System.out.println("-----json returned =" + e.getMessage());
        }
    }

    @Test
    public void getVpcRouteTables_pass () throws Exception {
        Router router = new Router();
        router.setId(UnitTestConfig.routerId);
        router.setVpcRouteTables(new ArrayList<>(){{add(new RouteTable(){{setRouteTableType(RouteTableType.VPC.getRouteTableType());setId(UnitTestConfig.routeTableId);}});}});

        VpcWebJson vpcWebJson = new VpcWebJson();
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcEntity.setRouter(router);
        vpcWebJson.setNetwork(vpcEntity);

        Mockito.when(vpcRouterToVpcService.getVpcWebJson(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcWebJson);
        this.mockMvc.perform(get(getVpcRouteTablesUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetables.length()").value(1));
    }

    @Test
    public void getVpcRouteTableById_pass () throws Exception {
        RouteTable routetable = new RouteTable();
        routetable.setId(UnitTestConfig.routeTableId);

        Mockito.when(routeTableDatabaseService.getByRouteTableId(UnitTestConfig.routeTableId))
                .thenReturn(routetable);
        this.mockMvc.perform(get(RouteTablesUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable.id").value(UnitTestConfig.routeTableId));;
    }

    @Test
    public void getSubnetRouteTable_pass () throws Exception {
        RouteTable routetable = new RouteTable();
        routetable.setId(UnitTestConfig.routeTableId);

        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>(){{put(UnitTestConfig.routeTableId, routetable);}});
        this.mockMvc.perform(get(subnetRouteTableUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable.id").value(UnitTestConfig.routerId));
    }

    @Test
    public void getSubnetRouteTable_ExistMultipleSubnetRouteTable_notPass () throws Exception {
        RouteTable routetable1 = new RouteTable();
        routetable1.setId(UnitTestConfig.routeTableId);
        RouteTable routetable2 = new RouteTable();
        routetable2.setId(UnitTestConfig.routeTableId2);

        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>() {
                    {
                        put(UnitTestConfig.routeTableId, routetable1);
                        put(UnitTestConfig.routeTableId2, routetable2);
                    }
                });
        try {
            this.mockMvc.perform(get(subnetRouteTableUri))
                    .andDo(print())
                    .andExpect(status().is(500));
        } catch (Exception e) {
            assertEquals("exist multiple subnet routetable searched by subnet id", e.getMessage());
            System.out.println("-----json returned =" + e.getMessage());
        }
    }

    @Test
    public void updateSubnetRouteTable_pass () throws Exception {
        RouteTable routetable = new RouteTable();
        routetable.setId(UnitTestConfig.routeTableId);
        routetable.setRouteEntities(new ArrayList<>());

        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>(){{put(UnitTestConfig.routeTableId, routetable);}});
        Mockito.when(neutronRouterService.updateRoutingRule(anyString(), any(NewRoutesWebRequest.class), anyBoolean()))
                .thenReturn(new UpdateRoutingRuleResponse() {
                    {
                        setHostRouteToSubnet(new ArrayList<>());
                        setInternalSubnetRoutingTable(new InternalSubnetRoutingTable() {
                            {
                                setRoutingRules(new ArrayList<>());
                            }
                        });
                    }
                });
        this.mockMvc.perform(put(subnetRouteTableUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.subnetRouteTableResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.routetable.id").value(UnitTestConfig.updateRouteTableId));
    }

    @Test
    public void deleteSubnetRouteTable_pass () throws Exception {
        RouteTable routetable = new RouteTable();
        routetable.setId(UnitTestConfig.routeTableId);

        Mockito.when(routeTableDatabaseService.getAllRouteTables(anyMap()))
                .thenReturn(new HashMap<String, RouteTable>(){{put(UnitTestConfig.routeTableId, routetable);}});
        Mockito.when(neutronRouterService.updateRoutingRule(anyString(), any(NewRoutesWebRequest.class), anyBoolean()))
                .thenReturn(new UpdateRoutingRuleResponse(){{setHostRouteToSubnet(new ArrayList<>());}});
        this.mockMvc.perform(delete(subnetRouteTableUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.routeTableId));
    }

}
