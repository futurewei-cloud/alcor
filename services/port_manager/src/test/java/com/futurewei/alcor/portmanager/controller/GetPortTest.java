/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
    private String getSubnetPortsUrl = "/project/" + UnitTestConfig.projectId + "/subnet-port-count/" + UnitTestConfig.subnetId;

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

    @Test
    public void getSubnetPortsTest() throws Exception {
        this.mockMvc.perform(get(getSubnetPortsUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
