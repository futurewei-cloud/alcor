package com.futurewei.alcor.gatewaymanager.controller;

import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.gatewaymanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.restclient.GatewayManagerRestClinet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScans(value = {@ComponentScan("com.futurewei.alcor.web.restclient"),
        @ComponentScan("com.futurewei.alcor.common.test.config")})
@AutoConfigureMockMvc
public class GatewayControllerTest extends MockIgniteServer {

    private final String url_createGatewayInfo = "/project/" + UnitTestConfig.projectId + "/gatewayinfo";
    private final String url_forUpdateAndDelete = "/project/" + UnitTestConfig.projectId + "/gatewayinfo/" + UnitTestConfig.vpcId;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayManagerRestClinet gatewayManagerRestClinet;

    @Test
    public void createGatewayInfoTest() throws Exception {

        Mockito.when(gatewayManagerRestClinet.createDPMCacheGateway(eq(UnitTestConfig.projectId), eq(UnitTestConfig.buildResourceGatewayInfoPending())))
                .thenReturn("success");

        Mockito.when(gatewayManagerRestClinet.createVPCInZetaGateway(UnitTestConfig.buildResourceVpcInfoSub()))
                .thenReturn(UnitTestConfig.buildResourceZetaGatewayIpJson());

        doNothing().when(gatewayManagerRestClinet).updateDPMCacheGateway(eq(UnitTestConfig.projectId), any(GatewayInfo.class));

        mockMvc.perform(post(url_createGatewayInfo).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcInfoJson()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void updateGatewayInfoForZetaTest() throws Exception {
        mockMvc.perform(put(url_forUpdateAndDelete).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.updateGatewayInfo()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
    }

    @Test
    public void deleteGatewayInfoForZetaTest() throws Exception {

        doNothing().when(gatewayManagerRestClinet).deleteVPCInZetaGateway(UnitTestConfig.vpcId);

        doNothing().when(gatewayManagerRestClinet).deleteDPMCacheGateway(UnitTestConfig.projectId, UnitTestConfig.vpcId);

        mockMvc.perform(delete(url_forUpdateAndDelete))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
    }
}
