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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class GetPortTest extends MockRestClientAndRepository {
    @Autowired
    private MockMvc mockMvc;

    private String listPortUrl = "/project/" + UnitTestConfig.projectId + "/ports";
    private String getPortUrl = "/project/" + UnitTestConfig.projectId + "/ports/" + UnitTestConfig.portId1;

    @Test
    public void getPortTest() throws Exception {
        this.mockMvc.perform(get(getPortUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.port.id")
                        .value(UnitTestConfig.portId1)
                );
    }

    @Test
    public void listPortTest() throws Exception {
        this.mockMvc.perform(get(listPortUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.ports[0].id")
                        .value(UnitTestConfig.portId1)
                );
    }
}
