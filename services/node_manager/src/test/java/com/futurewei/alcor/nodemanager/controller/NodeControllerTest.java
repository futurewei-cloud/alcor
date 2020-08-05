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
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
//@RunWith(MockitoJUnitRunner.class)
public class NodeControllerTest extends MockIgniteServer {
    private static final ObjectMapper om = new ObjectMapper();

    @MockBean
    NodeRepository mockNodeRepository;

    @MockBean
    private NodeService nodeService;

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
    public void test_index_wrongUrl() throws Exception {
        this.mockMvc.perform(get("/static/start.html"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_getNodeInfoFromUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "./machine.json", "application/json", "{\"host_info\":{\"node_id\":\"ephost_0\",\"node_name\":\"ephost_0\",\"local_ip\":\"172.17.0.6\",\"mac_address\":\"02:42:ac:11:00:06\",\"veth\":\"\",\"server_port\":50001}}".getBytes());
        List<NodeInfo> nodeList = new ArrayList<NodeInfo>();
        doNothing().when(mockNodeRepository).addItemBulkTransaction(nodeList);
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
        String strId = "h01";
        String strName = "host1";
        String strIp = "10.0.0.1";
        String strMac = "AA-BB-CC-01-01-01";
        String strVeth = "eth0";
        int nServerPort = 50001;
        NodeInfo nodeInfo = new NodeInfo(strId, strName, strIp, strMac, strVeth, nServerPort);
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        when(nodeService.createNodeInfo(any())).thenReturn(nodeInfo);
        mockMvc.perform(post("/nodes")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.node_id").value(strId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.node_name").value(strName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.local_ip").value(strIp))
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.mac_address").value(strMac))
                .andReturn();
    }

    @Test
    public void test_createNodeInfo_invalidInput_ip() throws Exception {
        String ip = "10, 0, 0, 1";
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        try {
            this.mockMvc.perform(post("/nodes")
                    .content(json)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("InvalidDataException"));
        }
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
        String ip = "10.0.0.2";
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        when(nodeService.updateNodeInfo(anyString(), any())).thenReturn(nodeInfo);
        doNothing().when(mockNodeRepository).addItem(nodeInfo);
        this.mockMvc.perform(put("/nodes/h02")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateNodeInfo_invalidInput() throws Exception {
        String ip = "10.0.0.2";
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
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
        when(nodeService.getNodeInfoById(anyString())).thenReturn(nodeInfo);
        MvcResult result = this.mockMvc.perform(get("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.host_info.node_id").value(strNodeId))
                .andReturn();
    }

    @Test
    public void test_getNodeInfoByNodeId_invalidId() throws Exception {
        String ip = "10.0.0.3";
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", ip, "AA-BB-CC-03-03-03");
        String strNodeId = "       ";
        try {
            MvcResult result = this.mockMvc.perform(get("/nodes/" + strNodeId))
                    .andDo(print())
                    .andReturn();
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("ParameterUnexpectedValueException"));
        }
    }

    @Test
    public void test_getAllNodes() throws Exception {
        HashMap<String, NodeInfo> hashMap = new HashMap<String, NodeInfo>();

        NodeInfo nodeInfo1 = new NodeInfo("h01", "host1", "10.0.0.1", "AA-BB-CC-03-03-01");
        NodeInfo nodeInfo2 = new NodeInfo("h02", "host2", "10.0.0.2", "AA-BB-CC-03-03-02");
        NodeInfo nodeInfo3 = new NodeInfo("h03", "host3", "10.0.0.3", "AA-BB-CC-03-03-03");
        hashMap.put(nodeInfo1.getId(), nodeInfo1);
        hashMap.put(nodeInfo2.getId(), nodeInfo2);
        hashMap.put(nodeInfo3.getId(), nodeInfo3);
        List<NodeInfo> nodes = new ArrayList<>();
        nodes.add(nodeInfo1);
        nodes.add(nodeInfo2);
        nodes.add(nodeInfo3);
        when(nodeService.getAllNodes(anyMap())).thenReturn(nodes);
        MvcResult result = this.mockMvc.perform(get("/nodes/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
        String strResult = result.getResponse().getContentAsString();
        assertTrue(strResult.indexOf(nodeInfo1.getId()) >= 0 && strResult.indexOf(nodeInfo2.getId()) >= 0 && strResult.indexOf(nodeInfo3.getId()) >= 0);
    }

    @Test
    public void deleteNodeInfo() throws Exception {
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", "10.0.0.3", "AA-BB-CC-03-03-03");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        String strNodeId = "h03";
        when(mockNodeRepository.findItem(nodeInfo.getId())).thenReturn(nodeInfo);
        doNothing().when(mockNodeRepository).deleteItem(strNodeId);
        MvcResult result = this.mockMvc.perform(delete("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(strNodeId));
    }

    @Test
    public void deleteNodeInfo_invalidId() throws Exception {
        String strNodeId = "  ";
        doNothing().when(mockNodeRepository).deleteItem(strNodeId);
        try {
            MvcResult result = this.mockMvc.perform(delete("/nodes/" + strNodeId))
                    .andDo(print())
                    .andReturn();
            assertEquals(19, result.getResponse().getContentAsString().length());
        } catch (Exception e) {
            assertTrue(e.getCause().getClass().getSimpleName().contains("ParameterNullOrEmptyException"));
        }
    }

    @Test
    public void testNodeBulkRegistration() throws Exception {
        Gson gson=new Gson();
        String input =
        "{\"nodeInfos\":[{\"id\":\"c2b79aca-316e-4ce8-a8ac-815e2de1f129\",\"name\":\"compute9\",\"localIp\":\"10.213.43.150\",\"macAddress\":\"90:17:ac:c1:34:5d\",\"veth\":\"eth1\",\"gRPCServerPort\":8080},{\"id\":\"c2b79aca-316e-4ce8-a8ac-815e2de1f120\",\"name\":\"compute10\",\"localIp\":\"10.213.43.151\",\"macAddress\":\"90:17:ac:c1:34:5e\",\"veth\":\"eth1\",\"gRPCServerPort\":8080}]}";
        BulkNodeInfoJson bulkNodeInfoJson = gson.fromJson(input, BulkNodeInfoJson.class);

        assertEquals(
                nodeService.createNodeInfoBulk(bulkNodeInfoJson.getNodeInfos()).size(),
        nodeService.getAllNodes().size());
           }
}