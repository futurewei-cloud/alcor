package com.futurewei.alcor.route;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

import com.futurewei.alcor.route.config.UnitTestConfig;
import com.futurewei.alcor.route.service.RouteDatabaseService;
import com.futurewei.alcor.web.entity.route.RouteWebObject;
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


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class RouteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteDatabaseService routeDatabaseService;

    private String getByIdUri = "/vpcs/" + UnitTestConfig.vpcId + "/routes/" + UnitTestConfig.routeId;
    private String createSubnetUri = "/subnets/" + UnitTestConfig.subnetId + "/routes";
    private String createVpcUri = "/vpcs/" + UnitTestConfig.vpcId + "/routes";
    private String deleteUri = "/vpcs/" + UnitTestConfig.vpcId + "/routes/" + UnitTestConfig.routeId;

    @Test
    public void routeGetById_canFindRoute_pass () throws Exception {
        Mockito.when(routeDatabaseService.getByRouteId(UnitTestConfig.routeId))
                .thenReturn(new RouteWebObject(){{setId(UnitTestConfig.routeId);}});
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.route.id").value(UnitTestConfig.routeId));
    }

    @Test
    public void routeGetById_canNotFindRoute_notPass () throws Exception {
        Mockito.when(routeDatabaseService.getByRouteId(UnitTestConfig.routeId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"route\":null}", response);
    }

    @Test
    public void createVpcRoute_create_pass () throws Exception {
        this.mockMvc.perform(post(createVpcUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.vpcResource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.route.destination").value(UnitTestConfig.cidr));
    }

    @Test
    public void createVpcRoute_parameterNullOrEmpty_notPass () throws Exception {
        try {
            this.mockMvc.perform(post(createVpcUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.vpcResource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.route.destination").value(UnitTestConfig.cidr));
        } catch (Exception e) {
            assertEquals("{\"route\":null}", e.getMessage());
        }
    }

    @Test
    public void createSubnetRoute_create_pass () throws Exception {
        this.mockMvc.perform(post(createSubnetUri).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.resource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.route.destination").value(UnitTestConfig.cidr));
    }

    @Test
    public void createSubnetRoute_parameterNullOrEmpty_notPass () throws Exception {
        try {
            this.mockMvc.perform(post(createSubnetUri).contentType(MediaType.APPLICATION_JSON)
                    .content(UnitTestConfig.resource))
                    .andDo(print())
                    .andExpect(status().is(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.route.destination").value(UnitTestConfig.cidr));
        } catch (Exception e) {
            assertEquals("{\"route\":null}", e.getMessage());
        }
    }

    @Test
    public void deleteRuleById_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(routeDatabaseService.getByRouteId(UnitTestConfig.routeId))
                .thenReturn(new RouteWebObject(){{setId(UnitTestConfig.routeId);}});
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.routeId));
    }

    @Test
    public void deleteRuleById_deleteWhenIdNotExist_notPass () throws Exception {
        Mockito.when(routeDatabaseService.getByRouteId(UnitTestConfig.routeId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
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
