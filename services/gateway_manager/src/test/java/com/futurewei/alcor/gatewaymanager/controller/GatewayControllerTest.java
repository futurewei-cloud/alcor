package com.futurewei.alcor.gatewaymanager.controller;

import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.gatewaymanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.restclient.GatewayManagerRestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${zetaGateway.enabled}")
    private boolean zetaGatewayEnabled;

    private final String url_createGatewayInfo = "/project/" + UnitTestConfig.projectId + "/gatewayinfo";
    private final String url_forUpdateAndDelete = "/project/" + UnitTestConfig.projectId + "/gatewayinfo/" + UnitTestConfig.vpcId;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayManagerRestClient gatewayManagerRestClient;

    @Test
    public void createGatewayInfoTest() throws Exception {

        Mockito.when(gatewayManagerRestClient.createDPMCacheGateway(eq(UnitTestConfig.projectId), eq(UnitTestConfig.buildResourceGatewayInfoPending())))
                .thenReturn("success");

        Mockito.when(gatewayManagerRestClient.createVPCInZetaGateway(UnitTestConfig.buildResourceVpcInfoSub()))
                .thenReturn(UnitTestConfig.buildResourceZetaGatewayIpJson());

        doNothing().when(gatewayManagerRestClient).updateDPMCacheGateway(eq(UnitTestConfig.projectId), any(GatewayInfo.class));

        if (zetaGatewayEnabled) {
            mockMvc.perform(post(url_createGatewayInfo).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcInfoJson()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
        } else {
            mockMvc.perform(post(url_createGatewayInfo).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.vpcInfoJson()))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }
    }

    @Test
    public void updateGatewayInfoForZetaTest() throws Exception {
        if (zetaGatewayEnabled) {
            mockMvc.perform(put(url_forUpdateAndDelete).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.updateGatewayInfo()))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
        } else {
            mockMvc.perform(put(url_forUpdateAndDelete).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.updateGatewayInfo()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    public void deleteGatewayInfoForZetaTest() throws Exception {

        doNothing().when(gatewayManagerRestClient).deleteVPCInZetaGateway(UnitTestConfig.vpcId);

        doNothing().when(gatewayManagerRestClient).deleteDPMCacheGateway(UnitTestConfig.projectId, UnitTestConfig.vpcId);

        if (zetaGatewayEnabled) {
            mockMvc.perform(delete(url_forUpdateAndDelete))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.id").value(UnitTestConfig.vpcId));
        } else {
            mockMvc.perform(delete(url_forUpdateAndDelete))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }
}
