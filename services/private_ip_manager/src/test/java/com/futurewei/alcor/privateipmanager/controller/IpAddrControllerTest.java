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
package com.futurewei.alcor.privateipmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRangeRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequestBulk;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class IpAddrControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createIpAddrRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = new IpAddrRangeRequest("range1", "subnet1", 4, "11.11.11.1", "11.11.11.254");
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRangeJson = objectMapper.writeValueAsString(ipAddrRangeRequest);

        this.mockMvc.perform(post("/ips/range/")
                .content(ipAddrRangeJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void deleteIpAddrRange() throws Exception {
        this.mockMvc.perform(delete("/ips/range/range1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getIpAddrRangeTest() throws Exception {
        this.mockMvc.perform(get("/ips/range/range1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void listIpAddrRangeTest() throws Exception {
        this.mockMvc.perform(get("/ips/range/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void allocateIpAddrTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(4, "range1", null, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(post("/ips")
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void allocateIpAddrBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(4, "range1", null, null);
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(4, "range1", null, null);
        IpAddrRequest ipAddrRequest3 = new IpAddrRequest(4, "range2", null, null);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);
        ipAddrRequests.add(ipAddrRequest3);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpAddrRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(post("/ips/bulk")
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void activateIpAddrStateTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(4, "range1", "11.11.11.1", "activate");
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(put("/ips")
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void activateIpAddrStateBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(4, "range1", "11.11.11.1", "activate");
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(4, "range1", "11.11.11.2", "activate");

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpAddrRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(put("/ips/bulk")
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void deactivateIpAddrStateTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(4, "range1", "11.11.11.1", "deactivate");
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(put("/ips")
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void deactivateIpAddrStateBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(4, "range1", "11.11.11.1", "deactivate");
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(4, "range1", "11.11.11.2", "deactivate");

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpAddrRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(put("/ips/bulk")
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void releaseIpAddrTest() throws Exception {
        this.mockMvc.perform(delete("/ips/4/range1/11.11.11.1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void releaseIpAddrBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(4, "range1", "11.11.11.1", "deactivate");
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(4, "range1", "11.11.11.2", "deactivate");

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpAddrRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(delete("/ips/bulk")
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getIpAddrTest() throws Exception {
        this.mockMvc.perform(get("/ips/4/range1/11.11.11.1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void listIpAddrTest() throws Exception {
        this.mockMvc.perform(get("/ips/4/range1"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
