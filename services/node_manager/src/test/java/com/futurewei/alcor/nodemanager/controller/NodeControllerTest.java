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
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.entity.NodeInfoJson;
import com.futurewei.alcor.nodemanager.service.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.InetAddress;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NodeControllerTest {
    private static final ObjectMapper om = new ObjectMapper();
    public NodeInfo testNodeInfo;
    String strTestNodeId = "";

    @Autowired
    NodeService service;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeController mockController;

    @Before
    public void init() {
        byte[] ip = new byte[]{10, 0, 0, 1};
        NodeInfo nodeInfo = new NodeInfo("h00", "host0", ip, "AA-BB-CC-DD-EE-00");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(nodeInfoJson);
            NodeInfo nodeInfo2 = service.createNodeInfo(nodeInfo);
            strTestNodeId = nodeInfo2.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createNodeInfo(NodeInfo nodeInfo) {
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String strNodeId = "";
        try {
            String json = objectMapper.writeValueAsString(nodeInfoJson);
            NodeInfo nodeInfo2 = service.createNodeInfo(nodeInfo);
            strTestNodeId = nodeInfo2.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strTestNodeId;
    }

    @Test
    public void test_index() throws Exception {
        this.mockMvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_getNodeInfoFromUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "@./machine.json", "application/json", "{\"host_info\":{\"node_id\":\"ephost_0\",\"node_name\":\"ephost_0\",\"local_ip\":\"172.17.0.6\",\"mac_address\":\"02:42:ac:11:00:06\",\"veth\":\"\",\"server_port\":50001}}" .getBytes());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/nodes/upload")
                .file(file))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void test_createNodeInfo() throws Exception {
        byte[] ip = new byte[]{10, 0, 0, 1};
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);

        this.mockMvc.perform(post("/nodes")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void updateNodeInfo() throws Exception {
        InetAddress address2 = InetAddress.getByName("10.0.0.2");
        byte[] ip = new byte[]{10, 0, 0, 2};
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = new NodeInfoJson(nodeInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);

        this.mockMvc.perform(put("/nodes/h01")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void test_getNodeInfoByNodeId() throws Exception {
        byte[] ip = new byte[]{10, 0, 0, 3};
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", ip, "AA-BB-CC-03-03-03");
        String strNodeId = createNodeInfo(nodeInfo);
        this.mockMvc.perform(get("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteNodeInfo() throws Exception {
        String strNodeId = "h00";
        this.mockMvc.perform(delete("/nodes/" + strNodeId))
                .andExpect(status().isOk());
    }

    @Test
    public void test_createNodeInfo_invalidInput() throws Exception {
        byte[] ip = new byte[]{10, 0, 0, 1};
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11");
        NodeInfoJson nodeInfoJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(post("/nodes")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateNodeInfo_invalidInput() throws Exception {
        InetAddress address2 = InetAddress.getByName("10.0.0.2");
        byte[] ip = new byte[]{10, 0, 0, 2};
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22");
        NodeInfoJson nodeInfoJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(nodeInfoJson);
        this.mockMvc.perform(put("/nodes/h01")
                .content(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}