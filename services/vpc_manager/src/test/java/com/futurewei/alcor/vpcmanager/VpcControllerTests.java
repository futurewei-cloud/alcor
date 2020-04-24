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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;

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

    @Test
    public void vpcGetById_canFindVpc_pass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcState(UnitTestConfig.projectId,
                        UnitTestConfig.vpcId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void vpcGetById_canNotFindVpc_notPass () throws Exception {
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
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
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vpc.id").value(UnitTestConfig.vpcId));

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
