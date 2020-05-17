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
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacRangeJson;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.entity.MacStateJson;
import com.futurewei.alcor.macmanager.service.MacService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MacControllerTest extends MockIgniteServer {
    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    MacService service;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MacController mockController;

    @Test
    void contextLoads() {
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
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void test_createMacState_invalidInput_null() throws Exception {
        MacState macState = new MacState("", "project3", "vpc3", "port3", "Active");
        MacStateJson macStateJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson);
        this.mockMvc.perform(post("/macs")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_getMacStateByMacAddress() throws Exception {
        MacState macState = new MacState("AA-BB-CC-01-01-01", "project1", "vpc1", "port2", "Active");
        MacStateJson macStateJson = new MacStateJson(macState);
        String strTestMac = "AA-BB-CC-01-01-01";
        Mockito.when(mockController.getMacStateByMacAddress(strTestMac)).thenReturn(macStateJson);
        this.mockMvc.perform(get("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.mac_address").value(strTestMac))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getMacStateByMacAddress_invalidMac() throws Exception {
        MacState macState = new MacState("AA-BB-CC-01-01-01", "project1", "vpc1", "port2", "Active");
        MacStateJson macStateJson = new MacStateJson(macState);
        String strTestMac = "AA-BB-CC-010101";
        MvcResult result = this.mockMvc.perform(get("/macs/" + strTestMac))
                .andDo(print())
                .andReturn();
        assertEquals(0, result.getResponse().getContentAsString().length());
    }

    @Test
    public void test_releaseMacStateByMacAddress() throws Exception {
        MacState macState = new MacState("AA-BB-CC-01-01-03", "project1", "vpc1", "port3", "Active");
        String strMacAddress = macState.getMacAddress();
        Mockito.when(mockController.deleteMacAllocation(strMacAddress)).thenReturn(macState.getMacAddress());
        this.mockMvc.perform(delete("/macs/" + strMacAddress))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_activateMacState() throws Exception {
        MacState macState1 = new MacState("AA-BB-CC-01-01-05", "project1", "vpc1", "port5", "Inactive");
        MacStateJson macStateJson1 = new MacStateJson(macState1);
        MacState macState2 = new MacState("AA-BB-CC-01-01-05", "project1", "vpc1", "port5", "Active");
        MacStateJson macStateJson2 = new MacStateJson(macState2);
        String strTestMac = macState1.getMacAddress();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson1);
        Mockito.when(mockController.updateMacState(strTestMac, macStateJson1)).thenReturn(macStateJson2);
        this.mockMvc.perform(put("/macs/" + strTestMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void test_deactivateMacState() throws Exception {
        MacState macState1 = new MacState("AA-BB-CC-01-01-05", "project1", "vpc1", "port5", "Active");
        MacStateJson macStateJson1 = new MacStateJson(macState1);
        MacState macState2 = new MacState("AA-BB-CC-01-01-05", "project1", "vpc1", "port5", "Inactive");
        MacStateJson macStateJson2 = new MacStateJson(macState2);
        String strTestMac = macState1.getMacAddress();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macStateJson1);
        Mockito.when(mockController.updateMacState(strTestMac, macStateJson1)).thenReturn(macStateJson2);
        this.mockMvc.perform(put("/macs/" + strTestMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void test_getMacRangeByMacRangeId() throws Exception {
        MacRange macRange = new MacRange("range1", "00-AA-BB-11-11-11", "00-AA-BB-11-11-FF", "Active");
        String strRangeId = macRange.getRangeId();
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        Mockito.when(mockController.getMacRangeByMacRangeId(strRangeId)).thenReturn(macRangeJson);
        this.mockMvc.perform(get("/macs/ranges/" + strRangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_getAllMacRanges() throws Exception {
        MacRange macRange2 = new MacRange("range2", "00-AA-BB-11-22-22", "00-AA-BB-11-22-FF", "Active");
        MacRange macRange3 = new MacRange("range3", "00-AA-BB-11-33-33", "00-AA-BB-11-22-FF", "Active");
        Collection<MacRange> ranges = new ArrayList<MacRange>();
        ranges.add(macRange2);
        ranges.add(macRange3);
        Map<String, Collection<MacRange>> map = new HashMap<String, Collection<MacRange>>();
        map.put("mac_ranges", ranges);
        Mockito.when(mockController.getAllMacRanges()).thenReturn(map);
        this.mockMvc.perform(get("/macs/ranges"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
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

    @Test
    public void updateMacRange() throws Exception {
        MacRange macRange = new MacRange("range5", "00-AA-BB-11-11-11", "00-AA-BB-55-55-55", "Inactive");
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        MacRange macRange2 = new MacRange("range5", "00-AA-BB-11-11-11", "00-AA-BB-55-55-55", "Active");
        MacRangeJson macRangeJson2 = new MacRangeJson(macRange2);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macRangeJson);
        Mockito.when(mockController.updateMacRange(macRange.getRangeId(), macRangeJson)).thenReturn(macRangeJson2);
        this.mockMvc.perform(put("/macs/ranges/range5")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteMacRange() throws Exception {
        MacRange macRange = new MacRange("range6", "00-AA-BB-11-22-22", "00-AA-BB-11-22-FF", "Active");
        String strRangeId = macRange.getRangeId();
        ResponseId responseId = new ResponseId(strRangeId);
        Mockito.when(mockController.deleteMacRange(strRangeId)).thenReturn(responseId);
        this.mockMvc.perform(delete("/macs/ranges" + strRangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteMacRange_invalidId() throws Exception {
        String strRangeId = "  ";
        MvcResult result = this.mockMvc.perform(delete("/macs/ranges" + strRangeId))
                .andDo(print())
                .andReturn();
        assertEquals(0, result.getResponse().getContentAsString().length());
    }
}

