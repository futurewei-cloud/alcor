package com.futurewei.alcor.route;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

import com.futurewei.alcor.route.config.UnitTestConfig;
import com.futurewei.alcor.route.entity.RouteState;
import com.futurewei.alcor.route.service.RouteDatabaseService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Test
    public void routeGetById_canFindRoute_pass () throws Exception {
        Mockito.when(routeDatabaseService.getByRouteId(UnitTestConfig.routeId))
                .thenReturn(new RouteState(){{setId(UnitTestConfig.routeId);}});
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

    @Before
    public void init() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }
}
