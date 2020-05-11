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
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.privateipmanager.config.UnitTestConfig;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRangeRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequestBulk;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IpAddrControllerTest extends MockIgniteServer {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void Test01_createIpAddrRangeTest() throws Exception {
        IpAddrRangeRequest ipAddrRangeRequest = new IpAddrRangeRequest(
                UnitTestConfig.rangeId,
                UnitTestConfig.subnetId,
                UnitTestConfig.ipVersion,
                UnitTestConfig.firstIp,
                UnitTestConfig.lastIp);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRangeJson = objectMapper.writeValueAsString(ipAddrRangeRequest);

        this.mockMvc.perform(post(UnitTestConfig.ipRangeUrl)
                .content(ipAddrRangeJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test02_getIpAddrRangeTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.ipRangeUrl + "/" + UnitTestConfig.rangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test03_listIpAddrRangeTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.ipRangeUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test04_allocateIpAddrTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                null,
                null);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(post(UnitTestConfig.ipAddrUrl)
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }


    @Test
    public void Test05_getIpAddrTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.ipAddrUrl + "/" +
                UnitTestConfig.rangeId + "/" + UnitTestConfig.ip1))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test06_deactivateIpAddrStateTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip1,
                UnitTestConfig.deactivated);
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(put(UnitTestConfig.ipAddrUrl)
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test07_activateIpAddrStateTest() throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip1,
                UnitTestConfig.activated);
        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestJson = objectMapper.writeValueAsString(ipAddrRequest);

        this.mockMvc.perform(put(UnitTestConfig.ipAddrUrl)
                .content(ipAddrRequestJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test08_releaseIpAddrTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.ipAddrUrl + "/" +
                UnitTestConfig.rangeId + "/" + UnitTestConfig.ip1))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test09_allocateIpAddrBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(4,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                null,
                null);
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(4,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                null,
                null);
        IpAddrRequest ipAddrRequest3 = new IpAddrRequest(4,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                null,
                null);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);
        ipAddrRequests.add(ipAddrRequest3);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(post(UnitTestConfig.ipAddrBulkUrl)
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void Test10_listIpAddrTest() throws Exception {
        this.mockMvc.perform(get(UnitTestConfig.ipAddrUrl + "/" + UnitTestConfig.rangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Test11_deactivateIpAddrStateBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip2,
                UnitTestConfig.deactivated);
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip3,
                UnitTestConfig.deactivated);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(put(UnitTestConfig.ipAddrBulkUrl)
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test12_activateIpAddrStateBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip2,
                UnitTestConfig.activated);
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip3,
                UnitTestConfig.activated);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(put(UnitTestConfig.ipAddrBulkUrl)
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test13_releaseIpAddrBulkTest() throws Exception {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip2,
                null);
        IpAddrRequest ipAddrRequest2 = new IpAddrRequest(
                UnitTestConfig.ipVersion,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip3,
                null);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        ObjectMapper objectMapper = new ObjectMapper();
        String ipAddrRequestBulkJson = objectMapper.writeValueAsString(ipAddrRequestBulk);

        this.mockMvc.perform(delete(UnitTestConfig.ipAddrBulkUrl)
                .content(ipAddrRequestBulkJson)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void Test14_deleteIpAddrRangeTest() throws Exception {
        this.mockMvc.perform(delete(UnitTestConfig.ipRangeUrl + "/" + UnitTestConfig.rangeId))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
