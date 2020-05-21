/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.nodemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Objects;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
public class NodeControllerTest extends MockIgniteServer {
    private static final ObjectMapper om = new ObjectMapper();
    @MockBean
    NodeService service;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeController mockController;

    @Test
    public void test_index() throws Exception {
        this.mockMvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_index_wrongUrl() throws Exception {
        this.mockMvc.perform(get("/static/start.html"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_getNodeInfoFromUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "@./machine.json", "application/json", "{\"host_info\":{\"node_id\":\"ephost_0\",\"node_name\":\"ephost_0\",\"local_ip\":\"172.17.0.6\",\"mac_address\":\"02:42:ac:11:00:06\",\"veth\":\"\",\"server_port\":50001}}".getBytes());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/nodes/upload")
                .file(file))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_getNodeInfoFromUpload_wrongFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file2", "", "application/json", "".getBytes());
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/nodes/upload")
                .file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect((rslt) -> assertTrue(rslt.getResolvedException().getClass() != null))
                .andReturn();
    }

    @Test
    public void test_createNodeInfo() throws Exception {
        String ip = "10, 0, 0, 1";
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(post("/nodes")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void test_createNodeInfo_invalidInput_null() throws Exception {
        String ip = "10, 0, 0, 1";
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11");
        NodeInfoJson nodeInfoJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(post("/nodes")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_createNodeInfo_invalidInputNull() throws Exception {
        NodeInfoJson nodeInfoJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(post("/nodes")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect((rslt) -> assertNotNull(Objects.requireNonNull(rslt.getResolvedException()).getClass()));
    }

    @Test
    public void updateNodeInfo() throws Exception {
        String ip = "10, 0, 0, 2";
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(put("/nodes/h01")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateNodeInfo_invalidInput() throws Exception {
        String ip = "10, 0, 0, 2";
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        Mockito.when(mockController.updateNodeInfo("h01", nodeInfoJson)).thenThrow(new ParameterUnexpectedValueException());
        try {
            this.mockMvc.perform(put("/nodes/h01")
                    .content(json)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("ParameterUnexpectedValueException"));
        }
    }

    @Test
    public void test_getNodeInfoByNodeId() throws Exception {
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", "10, 0, 0, 3", "AA-BB-CC-03-03-03");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        String strNodeId = "h03";
        Mockito.when(mockController.getNodeInfoById(strNodeId)).thenReturn(nodeInfoJson);
        MvcResult result = this.mockMvc.perform(get("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.node_id").value(strNodeId))
                .andReturn();
    }

    @Test
    public void test_getNodeInfoByNodeId_invalidId() throws Exception {
        String ip = "10, 0, 0, 3";
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", ip, "AA-BB-CC-03-03-03");
        String strNodeId = "       ";
        MvcResult result = this.mockMvc.perform(get("/nodes/" + strNodeId))
                .andDo(print())
                .andReturn();
        assertEquals(0, result.getResponse().getContentAsString().length());
    }

    @Test
    public void deleteNodeInfo() throws Exception {
        String strNodeId = "h00";
        Mockito.when(mockController.deleteNodeInfo(strNodeId)).thenReturn(strNodeId);
        MvcResult result = this.mockMvc.perform(delete("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(result.getResponse().getContentAsString(), strNodeId);
    }

    @Test
    public void deleteNodeInfo_invalidId() throws Exception {
        String strNodeId = "  ";
        MvcResult result = this.mockMvc.perform(delete("/nodes/" + strNodeId))
                .andDo(print())
                .andReturn();
        assertEquals(0, result.getResponse().getContentAsString().length());
    }
}