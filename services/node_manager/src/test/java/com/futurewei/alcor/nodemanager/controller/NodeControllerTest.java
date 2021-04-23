/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.nodemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class NodeControllerTest extends MockIgniteServer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CacheFactory cacheFactor;

    @MockBean
    private NodeService nodeService ;

    @Autowired
    WebApplicationContext context;

  @Before
  public void init1() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  public void testRegisterNodesInfoBulk() throws Exception {
    // do the cleanup to make sure the insertion happens
    nodeService.deleteNodeInfo("c2b79aca-316e-4ce8-a8ac-815e2de1f129");
    nodeService.deleteNodeInfo("c2b79aca-316e-4ce8-a8ac-815e2de1f120");
      String payLoad =
              "{\n"
                      + "  \"host_infos\":\n"
                      + "[{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\", \"node_name\": \"compute9\", \"local_ip\": \"10.213.43.150\", \"mac_address\": \"00:00:00:00:AB:c0\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                      + "}\n"
                      + ",\n"
                      + "{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\", \"node_name\": \"compute10\", \"local_ip\": \"10.213.43.151\", \"mac_address\": \"00:00:00:00:AB:CC\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                      + "}]\n"
                      + "}";
      final MvcResult mvcResult =
              this.mockMvc.perform(post("/nodes" + "/bulk").contentType(MediaType.APPLICATION_JSON)
                      .content(payLoad))
                      .andDo(print()).andExpect(status().isOk()).andReturn();
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
    String fileContent =
        "{\n"
            + "  \"host_infos\":\n"
            + "[{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\", \"node_name\": \"compute9\", \"local_ip\": \"10.213.43.150\", \"mac_address\": \"00:00:00:00:AB:c0\", \"veth\": \"eth1\", \"server_port\": 8080\n"
            + "}\n"
            + ",\n"
            + "{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\", \"node_name\": \"compute10\", \"local_ip\": \"10.213.43.151\", \"mac_address\": \"00:00:00:00:AB:CC\", \"veth\": \"eth1\", \"server_port\": 8080\n"
            + "}]\n"
            + "}";
        MockMultipartFile file = new MockMultipartFile("file", "./machine.json", "application/json",
                fileContent.getBytes());
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
        String strUnicastTopic = "unicast-topic-1";
        String strMulticastTopic = "multicast-topic-1";
        String strGroupTopic = "group-topic-1";
        NodeInfo nodeInfo = new NodeInfo(strId, strName, strIp, strMac, strVeth, nServerPort, strUnicastTopic, strMulticastTopic, strGroupTopic);
        String ncm_id = "ncm_001";
        nodeInfo.setNcmId(ncm_id);
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
                .andExpect(jsonPath("$.host_info.node_id").value(strId))
                .andExpect(jsonPath("$.host_info.node_name").value(strName))
                .andExpect(jsonPath("$.host_info.local_ip").value(strIp))
                .andExpect(jsonPath("$.host_info.mac_address").value(strMac))
                .andReturn();
    }

    @Test
    public void test_createNodeInfo_invalidInput_ip() throws Exception {
        String ip = "10, 0, 0, 1";
        NodeInfo nodeInfo = new NodeInfo("h01", "host1", ip, "AA-BB-CC-DD-EE-11", "unicast-topic-1", "multicast-topic-1", "group-topic-1");
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
    public void updateNodeInfo_invalidInput() throws Exception {
        String ip = "10.0.0.2";
        NodeInfo nodeInfo = new NodeInfo("h02", "host2", ip, "AA-BB-CC-DD-EE-22", "unicast-topic-1", "multicast-topic-1", "group-topic-1");
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
        String strNodeId = "c2b79aca-316e-4ce8-a8ac-815e2de1f129";
        //do insertion
                this.mockMvc
                        .perform(
                                post("/nodes" + "/bulk")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\n"
                                                        + "  \"host_infos\":\n"
                                                        + "[{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\", \"node_name\": \"compute9\", \"local_ip\": \"10.213.43.150\", \"mac_address\": \"00:00:00:00:AB:CC\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                                                        + "}\n"
                                                        + ",\n"
                                                        + "{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\", \"node_name\": \"compute10\", \"local_ip\": \"10.213.43.151\", \"mac_address\": \"00:00:00:00:AB:c0\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                                                        + "}]\n"
                                                        + "}"))
                        .andDo(print());
            //query the one inserted by id
        final MvcResult mvcResult =
                this.mockMvc.perform(get("/nodes/" + strNodeId)).andDo(print()).andExpect(status().isOk()).andReturn();
        System.out.println(mvcResult);
    }

    @Test
    public void test_getNodeInfoByNodeId_invalidId() throws Exception {
        String ip = "10.0.0.3";
        NodeInfo nodeInfo = new NodeInfo("h03", "host3", ip, "AA-BB-CC-03-03-03", "unicast-topic-1", "multicast-topic-1", "group-topic-1");
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
    this.mockMvc
        .perform(
            post("/nodes" + "/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\n"
                        + "  \"host_infos\":\n"
                        + "[{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\", \"node_name\": \"compute9\", \"local_ip\": \"10.213.43.150\", \"mac_address\": \"00:00:00:00:AB:CC\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                        + "}\n"
                        + ",\n"
                        + "{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\", \"node_name\": \"compute10\", \"local_ip\": \"10.213.43.151\", \"mac_address\": \"00:00:00:00:AB:c0\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                        + "}]\n"
                        + "}"))
        .andDo(print())
        .andExpect(status().isOk());

    this.mockMvc
        .perform(get("/nodes" + "").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

    @Test
    public void deleteNodeInfo() throws Exception {
                this.mockMvc
                        .perform(
                                post("/nodes" + "/bulk")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\n"
                                                        + "  \"host_infos\":\n"
                                                        + "[{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\", \"node_name\": \"compute9\", \"local_ip\": \"10.213.43.150\", \"mac_address\": \"00:00:00:00:AB:CC\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                                                        + "}\n"
                                                        + ",\n"
                                                        + "{ \"node_id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\", \"node_name\": \"compute10\", \"local_ip\": \"10.213.43.151\", \"mac_address\": \"00:00:00:00:AB:c0\", \"veth\": \"eth1\", \"server_port\": 8080\n"
                                                        + "}]\n"
                                                        + "}"))
                        .andDo(print()).
        andExpect(status().isOk());
        String strNodeId="c2b79aca-316e-4ce8-a8ac-815e2de1f129";
        MvcResult result = this.mockMvc.perform(delete("/nodes/" + strNodeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(strNodeId));
    }

    @Test
    public void deleteNodeInfo_invalidId() throws Exception {
        String strNodeId = "  ";
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
    Gson gson = new Gson();
    String input =
        "{\n"
            + "\t\"nodeInfos\": [{\n"
            + "\t\t\"id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f129\",\n"
            + "\t\t\"name\": \"compute9\",\n"
            + "\t\t\"localIp\": \"10.213.43.150\",\n"
            + "\t\t\"macAddress\": \"00:00:00:00:AB:CC\",\n"
            + "\t\t\"veth\": \"eth1\",\n"
            + "\t\t\"gRPCServerPort\": 8080\n"
            + "\t}, {\n"
            + "\t\t\"id\": \"c2b79aca-316e-4ce8-a8ac-815e2de1f120\",\n"
            + "\t\t\"name\": \"compute10\",\n"
            + "\t\t\"localIp\": \"10.213.43.151\",\n"
            + "\t\t\"macAddress\": \"00:00:00:00:AB:c0\",\n"
            + "\t\t\"veth\": \"eth1\",\n"
            + "\t\t\"gRPCServerPort\": 8080\n"
            + "\t}]\n"
            + "}";
    BulkNodeInfoJson bulkNodeInfoJson = gson.fromJson(input, BulkNodeInfoJson.class);

    assertEquals(
        nodeService.createNodeInfoBulk(bulkNodeInfoJson.getNodeInfos()).size(),
        nodeService.getAllNodes().size());
  }
}
