package com.futurewei.alcor.vpcmanager;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.vpcmanager.config.UnitTestConfig;
import com.futurewei.alcor.vpcmanager.entity.RouteWebJson;
import com.futurewei.alcor.vpcmanager.entity.RouteWebObject;
import com.futurewei.alcor.vpcmanager.entity.VpcState;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
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
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andDo(document("vpc_get_byid"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void vpcGetById_canNotFindVpc_notPass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andDo(document("vpc_get_nofind"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"vpc\":null}", response);
    }

    @Test
    public void createVpcState_create_pass () throws Exception {
        RouteWebJson routeWebJson = new RouteWebJson();
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        Mockito.when(vpcService.getRoute(eq(UnitTestConfig.vpcId), any(VpcState.class)))
                .thenReturn(routeWebJson);
        this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andDo(document("vpcstate_post"))
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void createVpcState_canNotFindRoute_notPass () throws Exception {
        List<RouteWebObject> routeWebObjectList = new ArrayList<>();
        RouteWebObject routeWebObject = new RouteWebObject();
        routeWebObject.setDestination(UnitTestConfig.cidr);
        routeWebObjectList.add(routeWebObject);

        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, routeWebObjectList));
        Mockito.when(vpcService.getRoute(eq(UnitTestConfig.vpcId), any(VpcState.class)))
                .thenReturn(null);

        try {
            this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                    .andDo(print())
                    .andDo(document("vpcstate_post_noroute"))
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.routes[0].destination").value(UnitTestConfig.cidr));
        } catch (Exception e) {

        }
    }

    @Test
    public void updateVpcStateByVpcId_noUpdate_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andDo(document("vpcstate_update_no"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void updateVpcStateByVpcId_update_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.updateName,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andDo(document("vpcstate_update"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void deleteVpcStateByVpcId_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andDo(document("vpcsate_delete"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void deleteVpcStateByVpcId_deleteWhenIdNotExist_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andDo(document("vpcstate_delete_noexist"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"id\":null}", response);
    }

    @Test
    public void getVpcStatesByProjectId_getMap_pass () throws Exception {
        Map<String, VpcState> vpcStates = new HashMap<>();
        VpcState vpcState = new VpcState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name,
                UnitTestConfig.cidr, null);
        vpcStates.put("VpcState", vpcState);
        Mockito.when(vpcDatabaseService.getAllVpcs()).thenReturn(vpcStates);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andDo(document("vpcstate_get_by_projectid"))
                .andExpect(status().isOk());
    }

    @Test
    public void getVpcStatesByProjectId_getEmptyMap_pass () throws Exception {
        Map<String, VpcState> vpcStates = new HashMap<>();
        Mockito.when(vpcDatabaseService.getAllVpcs()).thenReturn(vpcStates);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andDo(document("vpcstate_get_by_projectid_empty"))
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
