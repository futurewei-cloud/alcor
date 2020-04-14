package com.futurewei.alcor.subnet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.subnet.config.UnitTestConfig;
import com.futurewei.alcor.subnet.entity.*;
import com.futurewei.alcor.subnet.service.SubnetDatabaseService;
import com.futurewei.alcor.subnet.service.SubnetService;
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
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class SubnetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubnetDatabaseService subnetDatabaseService;

    @MockBean
    private SubnetService subnetService;

    private String getByIdUri = "/project/" + UnitTestConfig.projectId + "/subnets/" + UnitTestConfig.subnetId;
    private String creatwUri = "/project/" + UnitTestConfig.projectId + "/subnets";
    private String getByProjectIdAndVpcIdUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/subnets";
    private String deleteUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/subnets/" + UnitTestConfig.subnetId;
    private String putUri = "/project/" + UnitTestConfig.projectId + "/vpcs/" + UnitTestConfig.vpcId + "/subnets/" + UnitTestConfig.subnetId;

    @Test
    public void subnetGetByIdTest1 () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void subnetGetByIdTest2 () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"subnet\":null}", response);
    }

    @Test
    public void subnetCreateTest1 () throws Exception {
        SubnetState subnetState = new SubnetState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr);
        VpcState vpcState = new VpcState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.cidr, new ArrayList<RouteWebObject>(){{add(new RouteWebObject());}});

        VpcStateJson vpcStateJson = new VpcStateJson(vpcState);
        RouteWebJson routeWebJson = new RouteWebJson();

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetState);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(vpcStateJson);
        Mockito.when(subnetService.createRouteRules(UnitTestConfig.vpcId, vpcStateJson))
                .thenReturn(routeWebJson);

        this.mockMvc.perform(post(creatwUri).contentType(MediaType.APPLICATION_JSON).
                content(UnitTestConfig.resource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void subnetCreateTest2 () throws Exception {
        SubnetState subnetState = new SubnetState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                UnitTestConfig.name, UnitTestConfig.cidr);
        VpcState vpcState = new VpcState(UnitTestConfig.projectId,
                UnitTestConfig.vpcId, UnitTestConfig.name, UnitTestConfig.cidr, new ArrayList<RouteWebObject>(){{add(new RouteWebObject());}});

        VpcStateJson vpcStateJson = new VpcStateJson(vpcState);
        RouteWebJson routeWebJson = new RouteWebJson();

        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(subnetState);
        Mockito.when(subnetService.verifyVpcId(UnitTestConfig.projectId, UnitTestConfig.vpcId))
                .thenReturn(null);
        Mockito.when(subnetService.createRouteRules(UnitTestConfig.vpcId, vpcStateJson))
                .thenReturn(routeWebJson);
        try {
            this.mockMvc.perform(post(creatwUri).contentType(MediaType.APPLICATION_JSON).
                    content(UnitTestConfig.resource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
        }catch (Exception ex) {
            //System.out.println(ex.getMessage());
            assertEquals(UnitTestConfig.createException, ex.getMessage());
        }

    }

    @Test
    public void subnetUpdateTest1 () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId)).
                thenReturn(new SubnetState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.name, UnitTestConfig.cidr));
        this.mockMvc.perform(put(putUri).contentType(MediaType.APPLICATION_JSON).
                content(UnitTestConfig.resource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void subnetUpdateTest2 () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.name, UnitTestConfig.cidr))
                .thenReturn(new SubnetState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.subnetId,
                        UnitTestConfig.updateName, UnitTestConfig.cidr));
        this.mockMvc.perform(put(putUri).contentType(MediaType.APPLICATION_JSON).
                content(UnitTestConfig.updateResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void subnetUpdateTest3 () throws Exception {
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
    public void subnetGetByProjectIdAndVpcIdTest1 () throws Exception {
        Map<String, SubnetState> subnetStates = new HashMap<>();
        SubnetState subnetState = new SubnetState( UnitTestConfig.projectId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.name,UnitTestConfig.cidr);
        subnetStates.put("SubnetState", subnetState);
        Mockito.when(subnetDatabaseService.getAllSubnets()).thenReturn(subnetStates);
        this.mockMvc.perform(get(getByProjectIdAndVpcIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void subnetGetByProjectIdAndVpcIdTest2 () throws Exception {
        Map<String, SubnetState> subnetStates = new HashMap<>();
        Mockito.when(subnetDatabaseService.getAllSubnets()).thenReturn(subnetStates);
        this.mockMvc.perform(get(getByProjectIdAndVpcIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void subnetDeleteTest1 () throws Exception {
        Mockito.when(subnetDatabaseService.getBySubnetId(UnitTestConfig.subnetId))
                .thenReturn(new SubnetState( UnitTestConfig.projectId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.name,UnitTestConfig.cidr));
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.subnetId));
    }

    @Test
    public void subnetDeleteTest2 () throws Exception {
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
