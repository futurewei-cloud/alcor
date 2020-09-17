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
import com.futurewei.alcor.macmanager.dao.MacRangeMappingRepository;
import com.futurewei.alcor.web.entity.mac.*;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.macmanager.dao.MacRangeRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import org.junit.After;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MacControllerTest extends MockIgniteServer {
    private static final ObjectMapper om = new ObjectMapper();

    @MockBean
    MacRangeRepository mockMacRangeRepository;

    @MockBean
    MacStateRepository mockMacStateRepository;

    @MockBean
    MacRangeMappingRepository macRangeMappingRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test_index() throws Exception {
        this.mockMvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_createMacState() throws Exception {
        String strRangeId = "range0";
        String strMac = "AA-BB-CC-01-01-01";
        String strProjectId = "project1";
        String strVpc = "vpc1";
        String strPort = "port1";
        String strState = "Active";

        MacRange macRange = new MacRange(strRangeId, "AA-BB-CC-00-00-00", "AA-BB-CC-FF-FF-FF", "Active");
        MacState macState1 = new MacState("", strProjectId, strVpc, strPort, strState);
        MacStateJson macStateJson1 = new MacStateJson(macState1);
        String json = om.writeValueAsString(macStateJson1);
        when(mockMacRangeRepository.findItem(strRangeId)).thenReturn(macRange);
        when(mockMacStateRepository.putIfAbsent(any())).thenReturn(true);
        when(macRangeMappingRepository.getUsedCapacity(strRangeId)).thenReturn(0L);
        when(macRangeMappingRepository.putIfAbsent(any(String.class), anyLong())).thenReturn(true);
        MvcResult result = this.mockMvc.perform(post("/macs")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.mac_address").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.project_id").value(strProjectId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.vpc_id").value(strVpc))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.port_id").value(strPort))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.state").value(strState))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().length() > 0);
    }

    @Test
    public void test_createMacState_invalidInput_null() throws Exception {
        MacStateJson macStateJson = null;
        String json = om.writeValueAsString(macStateJson);
        this.mockMvc.perform(post("/macs")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_getMacStateByMacAddress() throws Exception {
        MacState macState = new MacState("AA-BB-CC-01-01-01", "project1", "vpc1", "port2", "Active");
        String strTestMac = "AA-BB-CC-01-01-01";
        when(mockMacStateRepository.findItem(strTestMac)).thenReturn(macState);
        this.mockMvc.perform(get("/macs/" + strTestMac))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.mac_address").value(strTestMac));
    }

    @Test
    public void test_getMacStateByMacAddress_invalidMac() throws Exception {
        MacState macState = new MacState("AA-BB-CC-01-01-01", "project1", "vpc1", "port2", "Active");
        String strTestMac = "AA-BB-CC-010101";
        try {
            this.mockMvc.perform(get("/macs/" + strTestMac))
                    .andDo(print());
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("MacAddressInvalidException"));
        }
    }

    @Test
    public void test_releaseMacStateByMacAddress() throws Exception {
        String strRangeId = "range0";
        String strMac = "AA-BB-CC-01-01-01";
        String strProjectId = "project1";
        String strVpc = "vpc1";
        String strPort = "port1";
        String strState = "Active";
        MacRange macRange = new MacRange("range0", "AA-BB-CC-00-00-00", "AA-BB-CC-FF-FF-FF", "Active");
        MacState macState = new MacState("", strProjectId, strVpc, strPort, strState);
        when(mockMacStateRepository.findItem(strMac)).thenReturn(macState);
        doNothing().when(mockMacStateRepository).deleteItem(strMac);
        doNothing().when(mockMacRangeRepository).addItem(macRange);
        when(mockMacRangeRepository.findItem(strRangeId)).thenReturn(macRange);
        this.mockMvc.perform(delete("/macs/" + strMac))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_address").doesNotExist())
                .andReturn();
    }

    @Test
    public void test_activateMacState() throws Exception {
        String strMac = "AA-BB-CC-01-01-04";
        String strProjectId = "project1";
        String strVpc = "vpc1";
        String strPort = "port4";
        String strState = "Inactive";
        String strState2 = "Active";
        MacState macState1 = new MacState(strMac, strProjectId, strVpc, strPort, strState);
        MacState macState2 = new MacState(strMac, strProjectId, strVpc, strPort, strState2);
        MacStateJson macStateJson2 = new MacStateJson(macState2);
        String json = om.writeValueAsString(macStateJson2);
        when(mockMacStateRepository.findItem(macState1.getMacAddress())).thenReturn(macState2);
        doNothing().when(mockMacStateRepository).addItem(macState2);
        this.mockMvc.perform(put("/macs/" + strMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.mac_address").value(strMac))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.project_id").value(strProjectId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.vpc_id").value(strVpc))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.port_id").value(strPort))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.state").value(strState2))
                .andDo(print());
    }

    @Test
    public void test_deactivateMacState() throws Exception {
        String strMac = "AA-BB-CC-01-01-05";
        String strProjectId = "project1";
        String strVpc = "vpc1";
        String strPort = "port5";
        String strState2 = "Inactive";
        MacState macState2 = new MacState(strMac, strProjectId, strVpc, strPort, strState2);
        MacStateJson macStateJson2 = new MacStateJson(macState2);
        String json = om.writeValueAsString(macStateJson2);
        when(mockMacStateRepository.findItem(strMac)).thenReturn(macState2);
        doNothing().when(mockMacStateRepository).addItem(macState2);
        this.mockMvc.perform(put("/macs/" + strMac)
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.mac_address").value(strMac))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.project_id").value(strProjectId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.vpc_id").value(strVpc))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.port_id").value(strPort))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_state.state").value(strState2))
                .andDo(print());
    }

    @Test
    public void test_getMacRangeByMacRangeId() throws Exception {
        MacRange macRange = new MacRange("range1", "00-AA-BB-11-11-11", "00-AA-BB-11-11-FF", "Active");
        String strRangeId = macRange.getRangeId();
        when(mockMacRangeRepository.findItem(strRangeId)).thenReturn(macRange);
        this.mockMvc.perform(get("/macs/ranges/" + strRangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_getAllMacRanges() throws Exception {
        MacRange macRange2 = new MacRange("range2", "00-AA-BB-11-22-22", "00-AA-BB-11-22-FF", "Active");
        MacRange macRange3 = new MacRange("range3", "00-AA-BB-11-33-33", "00-AA-BB-11-22-FF", "Active");
        Map<String, MacRange> map = new Hashtable<>();
        map.put(macRange2.getRangeId(), macRange2);
        map.put(macRange3.getRangeId(), macRange3);
        when(mockMacRangeRepository.findAllItems()).thenReturn(map);
        this.mockMvc.perform(get("/macs/ranges"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mac_ranges[0].range_id").value("range2"));
    }

    @Test
    public void test_createMacRange() throws Exception {
        MacRange macRange = new MacRange("range4", "00-AA-BB-11-00-11", "00-AA-BB-11-00-FF", "Active");
        MacRangeJson macRangeJson = new MacRangeJson(macRange);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(macRangeJson);
        doNothing().when(mockMacRangeRepository).addItem(macRange);
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
        String json = om.writeValueAsString(macRangeJson);
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
        doNothing().when(mockMacRangeRepository).deleteItem(strRangeId);
        this.mockMvc.perform(delete("/macs/ranges/" + strRangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteMacRange_invalidId() throws Exception {
        String strRangeId = "  ";
        try {
            MvcResult result = this.mockMvc.perform(delete("/macs/ranges" + strRangeId))
                    .andDo(print())
                    .andReturn();
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("MacAddressInvalidException"));
        }
    }

    @Before
    public void before() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }
}

