package com.futurewei.alcor.dataplane.controller;

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.NodeInfoCache;
import com.futurewei.alcor.dataplane.constants.NodeUnitTestConstant;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NodeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createNodeInfoTest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post(NodeUnitTestConstant.url)
                .content(NodeUnitTestConstant.create_node_test_input)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }
}
