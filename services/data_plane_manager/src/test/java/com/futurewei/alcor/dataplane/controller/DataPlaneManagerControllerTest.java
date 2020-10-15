package com.futurewei.alcor.dataplane.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.dataplane.utils.DataPlaneManagerUtil;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class DataPlaneManagerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void Test1() throws Exception {
        DataPlaneManagerUtil dataPlaneManagerUtil = new DataPlaneManagerUtil();

        String testUrl = "/dpm/port/";

        NetworkConfiguration input = dataPlaneManagerUtil.autoGenerateUTsInput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 2, false);
        Map<String, Goalstate.GoalState> output = dataPlaneManagerUtil.autoGenerateUTsOutput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 2, false);
        String input_json = new ObjectMapper().writeValueAsString(input);

        MvcResult result =  this.mockMvc
                .perform(post(testUrl).contentType(MediaType.APPLICATION_JSON).content(input_json))
                .andReturn();


    }
}
