/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.UnitTestConfig;
import com.futurewei.alcor.dataplane.utils.GoalStateManager;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class GSControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Config config;

    @MockBean
    private GoalStateManager goalStateManager;

    private String portUri = "/port/";
    private String subnetUri = "/subnet/";

    @Test
    public void createPort_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(post(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void createPort_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(post(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

    @Test
    public void updatePort_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(put(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void updatePort_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(put(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

    @Test
    public void deletePort_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(delete(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void deletePort_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(delete(portUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

    @Test
    public void createSubnet_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(post(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void createSubnet_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(post(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

    @Test
    public void updateSubnet_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(put(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void updateSubnet_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(put(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

    @Test
    public void deleteSubnet_pass () throws Exception {

        Mockito.when(config.getPort())
                .thenReturn(4);
        Mockito.when(config.getOvs())
                .thenReturn("true");

        this.mockMvc.perform(delete(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Successfully Handle request !!"));

    }

    @Test
    public void deleteSubnet_portEntitiesIsNull_notPass () throws Exception {

        this.mockMvc.perform(delete(subnetUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.scenario2v1_SingleFP_input))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultMessage").value("Failure Handle request reason: null"));

    }

}
