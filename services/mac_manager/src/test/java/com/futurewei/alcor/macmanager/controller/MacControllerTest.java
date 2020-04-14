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
package com.futurewei.alcor.macmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.entity.MacStateJson;
import com.futurewei.alcor.macmanager.service.MacService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Hashtable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MacControllerTest {
    private static final ObjectMapper om = new ObjectMapper();
    public MacState testMacState;
    String strTestMac = "";

    @Autowired
    MacService service;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MacController mockController;

    @Before
    public void init() {
        MacState macState = new MacState("", "project1", "vpc1", "port1", "Active");
        MacStateJson macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(macStateJson);
            MacState macState2 = service.createMacState(macState);
            strTestMac = macState2.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_index() throws Exception {
        this.mockMvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_createMacState() throws Exception {
        MacState macState = new MacState("", "project1", "vpc1", "port2", "Active");
        MacStateJson macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson);

        System.out.println(json);
        MvcResult mvcResult = this.mockMvc.perform(post("/macs")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void test_getMacStateByMacAddress() throws Exception {
        init();
        this.mockMvc.perform(get("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_releaseMacStateByMacAddress() throws Exception {
        init();
        this.mockMvc.perform(delete("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_activateMacState(String macaddress) throws Exception {
        init();
        this.mockMvc.perform(delete("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }

    MacState activateMacState(String macaddress) throws Exception;

    MacState deactivateMacState(String macaddress) throws Exception;

    String releaseMacState(String macAddress) throws Exception;

    MacRange getMacRangeByMacRangeId(String macRangeId);

    Hashtable<String, MacRange> getAllMacRanges();

    MacRange createMacRange(MacRange macRange) throws Exception;

    MacRange updateMacRange(MacRange macRange) throws Exception;

    String deleteMacRange(String rangeid) throws Exception;
}

