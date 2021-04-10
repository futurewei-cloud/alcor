/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
