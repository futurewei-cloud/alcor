package com.futurewei.alcor.vpcmanager;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.vpcmanager.config.UnitTestConfig;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
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
import java.util.List;
import java.util.Map;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class VpcControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VpcDatabaseService vpcDatabaseService;

    @MockBean
    private VpcService vpcService;

    private String getByIdUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId;
    private String createUri = "/project/" + UnitTestConfig.projectId + "/vpcs";
    private String updateUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId;
    private String deleteUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId;
    private String getByProjectIdUri = "/project/" + UnitTestConfig.projectId + "/vpcs";

    @Test
    public void vpcGetById_canFindVpc_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void vpcGetById_canNotFindVpc_notPass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"network\":null}", response);
    }

    @Test
    public void createVpcState_create_pass () throws Exception {
        RouteWebJson routeWebJson = new RouteWebJson();
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        Mockito.when(vpcService.getRoute(eq(UnitTestConfig.vpcId), any(VpcEntity.class)))
                .thenReturn(routeWebJson);
        Mockito.when(vpcService.allocateSegmentForNetwork(any(VpcEntity.class)))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.network.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void createVpcState_canNotFindRoute_notPass () throws Exception {
        List<RouteEntity> routeEntityList = new ArrayList<>();
        RouteEntity routeEntity = new RouteEntity();
        routeEntity.setDestination(UnitTestConfig.cidr);
        routeEntityList.add(routeEntity);

        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, routeEntityList));
        Mockito.when(vpcService.getRoute(eq(UnitTestConfig.vpcId), any(VpcEntity.class)))
                .thenReturn(null);

        Mockito.when(vpcService.registerVpc(any(VpcEntity.class))).thenReturn(new ResponseId(UnitTestConfig.vpcId));

        String response = this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andExpect(status().is(201)).andReturn().getResponse().getContentAsString();
        assertEquals("{\"network\":null}", response);
    }

    @Test
    public void updateVpcStateByVpcId_noUpdate_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void updateVpcStateByVpcId_update_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.updateName,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void deleteVpcStateByVpcId_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void deleteVpcStateByVpcId_deleteWhenIdNotExist_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"id\":null}", response);
    }

    @Test
    public void getVpcStatesByProjectId_getMap_pass () throws Exception {
        Map<String, VpcEntity> vpcStates = new HashMap<>();
        VpcEntity vpcState = new VpcEntity(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name,
                UnitTestConfig.cidr, null);
        vpcStates.put("VpcWebResponseObject", vpcState);
        Mockito.when(vpcDatabaseService.getAllVpcs()).thenReturn(vpcStates);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getVpcStatesByProjectId_getEmptyMap_pass () throws Exception {
        Map<String, VpcEntity> vpcStates = new HashMap<>();
        Mockito.when(vpcDatabaseService.getAllVpcs(any())).thenReturn(vpcStates);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andExpect(status().isOk());
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
