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
package com.futurewei.alcor.portmanager.controller;

import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class CreatePortTest extends MockRestClientAndRepository {
    @Autowired
    private MockMvc mockMvc;

    private String createPortUrl = "/project/" + UnitTestConfig.projectId + "/ports";
    private String createPortBulkUrl = createPortUrl + "/bulk";

    @Test
    public void createPortWithFixedIpsTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithFixedIps))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip1));
    }

    @Test
    public void createPortWithoutFixedIpsTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutFixedIps))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.fixed_ips[0].ip_address").value(UnitTestConfig.ip1));
    }

    @Test
    public void createPortWithMacAddressTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithMacAddress))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_address").value(UnitTestConfig.mac1));
    }

    @Test
    public void createPortWithoutMacAddressTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutMacAddress))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.mac_address").value(UnitTestConfig.mac1));
    }

    @Test
    public void createPortWithSecurityGroupTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithSecurityGroup))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.security_groups[0]").value(UnitTestConfig.securityGroupId1));
    }

    @Test
    public void createPortWithoutSecurityGroupTest() throws Exception {
        this.mockMvc.perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.portEntityWithoutSecurityGroup))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.port.id").value(UnitTestConfig.portId1));
    }

    @Test
    public void createPortBulkTest() throws Exception {
        this.mockMvc.perform(post(createPortBulkUrl).contentType(MediaType.APPLICATION_JSON)
                .content(UnitTestConfig.createPortBulk))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].id").value(UnitTestConfig.portId1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].id").value(UnitTestConfig.portId2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[0].fixed_ips[0].ip_address").value(UnitTestConfig.ip1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ports[1].fixed_ips[0].ip_address").value(UnitTestConfig.ip2));
    }
}
