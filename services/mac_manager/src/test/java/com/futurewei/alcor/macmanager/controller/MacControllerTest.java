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
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacRangeJson;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
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
import org.springframework.web.bind.annotation.PathVariable;

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

    public String createMacState(MacState macState) {
        MacStateJson macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();
        String strMacAddress = "";
        try {
            String json = objectMapper.writeValueAsString(macStateJson);
            MacState macState2 = service.createMacState(macState);
            strMacAddress = macState2.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMacAddress;
    }

    public String createMacRange(MacRange macRange) {
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        ObjectMapper objectMapper = new ObjectMapper();
        String strRangeId = "";

        try {
            String json = objectMapper.writeValueAsString(macRangeJson);
            MacRange macRange2 = service.createMacRange(macRange);
            strRangeId = macRange2.getRangeId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strRangeId;
    }

    @Test
    public void test_index() throws Exception {
        this.mockMvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_createMacState() throws Exception {
        MacState macState = new MacState("", "project3", "vpc3", "port3", "Active");
        MacStateJson macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson);

        this.mockMvc.perform(post("/macs")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void test_getMacStateByMacAddress() throws Exception {
        MacState macState = new MacState("", "project1", "vpc1", "port2", "Active");
        String strTestMac = createMacState(macState);
        this.mockMvc.perform(get("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_releaseMacStateByMacAddress() throws Exception {
        MacState macState = new MacState("", "project1", "vpc1", "port3", "Active");
        String strTestMac = createMacState(macState);
        this.mockMvc.perform(delete("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_activateMacState() throws Exception {
        MacState macState = new MacState("", "project1", "vpc1", "port5", "Inactive");
        String strTestMac = createMacState(macState);
        MacStateJson macStateJson = new MacStateJson(macState);
        System.out.println(macStateJson);
        macState = new MacState(strTestMac, "project1", "vpc1", "port5", "Active");
        macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson);

        this.mockMvc.perform(put("/macs/" + strTestMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void test_deactivateMacState() throws Exception {
        MacState macState = new MacState("", "project1", "vpc1", "port7", "Active");
        String strTestMac = createMacState(macState);
        MacStateJson macStateJson = new MacStateJson(macState);
        System.out.println(macStateJson);
        macState = new MacState(strTestMac, "project1", "vpc1", "port7", "Inactive");
        macStateJson = new MacStateJson(macState);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson);

        this.mockMvc.perform(put("/macs/" + strTestMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void test_getMacRangeByMacRangeId() throws Exception {
        MacRange macRange = new MacRange("range1", "00-AA-BB-11-11-11", "00-AA-BB-11-11-FF", "Active");
        String strRangeId = createMacRange(macRange);
        this.mockMvc.perform(get("/macs/ranges/" + strRangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public void test_getAllMacRanges(@PathVariable String rangeid) throws Exception {
        MacRange macRange = new MacRange("range2", "00-AA-BB-11-22-22", "00-AA-BB-11-22-FF", "Active");
        String strRangeId = createMacRange(macRange);
        macRange = new MacRange("range3", "00-AA-BB-11-33-33", "00-AA-BB-11-FF", "Active");
        strRangeId = createMacRange(macRange);
        this.mockMvc.perform(get("/macs/ranges"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public void test_createMacRange() throws Exception {
        MacRange macRange = new MacRange("range4", "00-AA-BB-11-00-11", "00-AA-BB-11-00-FF", "Active");
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macRangeJson);

        this.mockMvc.perform(post("/macs/ranges")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    public void updateMacRange() throws Exception {
        MacRange macRange = new MacRange("range5", "00-AA-BB-11-11-11", "00-AA-BB-55-55-55", "Inactive");
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macRangeJson);

        this.mockMvc.perform(put("/macs/ranges/range1")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    public void deleteMacRange() throws Exception {
        MacRange macRange = new MacRange("range6", "00-AA-BB-11-22-22", "00-AA-BB-11-22-FF", "Active");
        String strRangeId = createMacRange(macRange);
        System.out.println(strRangeId);
        this.mockMvc.perform(delete("/macs/ranges" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

