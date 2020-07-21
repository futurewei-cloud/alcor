/*
Copyright 2020 The Alcor Authors.

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
package com.futurewei.alcor.elasticipmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.elasticipmanager.config.UnitTestConfig;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAllocatedIpv4;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAllocatedIpv6;
import com.futurewei.alcor.elasticipmanager.entity.ElasticIpAvailableBucketsSet;
import com.futurewei.alcor.web.entity.elasticip.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.restclient.PortManagerRestClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class ElasticIpControllerTests extends MockIgniteServer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CacheFactory cacheFactor;

    @MockBean
    private PortManagerRestClient portManagerRestClient;

    private void perpareRange() throws Exception {
        try {
            // create ipv4 elastic ip range
            List<ElasticIpRange.AllocationRange> allocationRanges = new ArrayList<>();
            allocationRanges.add(new ElasticIpRange.AllocationRange(
                    UnitTestConfig.elasticIpRangeStart1,
                    UnitTestConfig.elasticIpRangeEnd1));

            ElasticIpRange eipRange = new ElasticIpRange(
                    UnitTestConfig.elasticIpRange1,
                    UnitTestConfig.elasticIpRangeName1,
                    UnitTestConfig.elasticIpRangeDescription1,
                    UnitTestConfig.elasticIpVersion1,
                    allocationRanges);

            ElasticIpRangeInfoWrapper rangeRequest = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(eipRange));
            ObjectMapper mapper = new ObjectMapper();
            String rangeRequestStr =  mapper.writeValueAsString(rangeRequest);
            String createRangeUri = "/elasticip-ranges";

            this.mockMvc.perform(post(createRangeUri).contentType(MediaType.APPLICATION_JSON)
                    .content(rangeRequestStr))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.id")
                            .value(UnitTestConfig.elasticIpRange1));

            // get the elastic ip range and check cidrs
            String getEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
            String responseStr = this.mockMvc.perform(get(getEipRangeUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            ElasticIpRangeInfoWrapper response = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
            Assert.assertEquals(UnitTestConfig.elasticIpRange1, response.getElasticIpRange().getId());
            Assert.assertEquals(UnitTestConfig.elasticIpRangeStart1,
                    response.getElasticIpRange().getAllocationRanges().get(0).getStart());
            Assert.assertEquals(UnitTestConfig.elasticIpRangeEnd1,
                    response.getElasticIpRange().getAllocationRanges().get(0).getEnd());

            // create ipv6 elastic ip range
            List<ElasticIpRange.AllocationRange> allocationRangesIpv6 = new ArrayList<>();
            allocationRangesIpv6.add(new ElasticIpRange.AllocationRange(
                    UnitTestConfig.elasticIpRangeStart2,
                    UnitTestConfig.elasticIpRangeEnd2));

            ElasticIpRange eipRangeIpv6 = new ElasticIpRange(
                    UnitTestConfig.elasticIpRange2,
                    UnitTestConfig.elasticIpRangeName2,
                    UnitTestConfig.elasticIpRangeDescription2,
                    UnitTestConfig.elasticIpVersion2,
                    allocationRangesIpv6);

            ElasticIpRangeInfoWrapper rangeRequestIpv6 = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(eipRangeIpv6));
            String rangeRequestIpv6Str = mapper.writeValueAsString(rangeRequestIpv6);

            this.mockMvc.perform(post(createRangeUri).contentType(MediaType.APPLICATION_JSON)
                    .content(rangeRequestIpv6Str))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.id")
                            .value(UnitTestConfig.elasticIpRange2));

            // get the ipv6 elastic ip range and check cidrs
            getEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
            responseStr = this.mockMvc.perform(get(getEipRangeUri))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            response = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
            Assert.assertEquals(UnitTestConfig.elasticIpRange2, response.getElasticIpRange().getId());
            Assert.assertEquals(UnitTestConfig.elasticIpRangeStart2,
                    response.getElasticIpRange().getAllocationRanges().get(0).getStart());
            Assert.assertEquals(UnitTestConfig.elasticIpRangeEnd2,
                    response.getElasticIpRange().getAllocationRanges().get(0).getEnd());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void cleanRange() throws Exception {
        try {
            String deleteRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
            this.mockMvc.perform(delete(deleteRangeUri))
                    .andDo(print())
                    .andExpect(status().isOk());

            deleteRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
            this.mockMvc.perform(delete(deleteRangeUri))
                    .andDo(print())
                    .andExpect(status().isOk());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void showIPv4AllocationInfo() throws Exception {
        try {
            ICache<String, ElasticIpAvailableBucketsSet> availableBuckets = cacheFactor.getCache(
                    ElasticIpAvailableBucketsSet.class);
            System.out.print("\nIpv4 available buckets: \n");
            for (Map.Entry<String, ElasticIpAvailableBucketsSet> item : availableBuckets.getAll().entrySet()) {
                System.out.print("Key " + item.getKey() + "\n");
                BitSet bitset = item.getValue().getAvailableBucketsBitset();
                int i = bitset.nextSetBit(0);
                while (i != -1 && i < 256 ) {
                    System.out.print(i + " ");
                    i = bitset.nextSetBit(i + 1);
                }
            }

            ICache<String, ElasticIpAllocatedIpv4> allocatedIpv4Cache = cacheFactor.getCache(ElasticIpAllocatedIpv4.class);
            for (Map.Entry<String, ElasticIpAllocatedIpv4> item : allocatedIpv4Cache.getAll().entrySet()) {
                System.out.print("\nIpv4 allocation bucket " + item.getKey() + "\n");
                ElasticIpAllocatedIpv4 loop = item.getValue();
                System.out.print("Ipv4 allocation bucket allocated ips: ");
                for (Long allocatedIp: loop.getAllocatedIps()) {
                    System.out.print(Ipv4AddrUtil.longToIpv4(allocatedIp) + ", ");
                }
                System.out.print("\n Ipv4 allocation bucket available ips: ");
                for (Long availableIp: loop.getAvailableIps()) {
                    System.out.print(Ipv4AddrUtil.longToIpv4(availableIp) + ", ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void showIPv6AllocationInfo() throws Exception {
        try {
            ICache<String, ElasticIpAllocatedIpv6> allocatedIpv6Cache = cacheFactor.getCache(ElasticIpAllocatedIpv6.class);
            System.out.print("\nIpv6 allocated ips: \n");
            for (Map.Entry<String, ElasticIpAllocatedIpv6> item : allocatedIpv6Cache.getAll().entrySet()) {
                ElasticIpAllocatedIpv6 loop = item.getValue();
                System.out.print(loop.getAllocatedIpv6());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void elasticIp_IpSpecified_create_update_delete() throws Exception {
        this.perpareRange();

        // create the elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv4Address1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);
        postRequest.setName(UnitTestConfig.elasticIpName1);
        postRequest.setDescription(UnitTestConfig.elasticIpDescription1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.elastic_ip")
                        .value(UnitTestConfig.elasticIpv4Address1))
                .andReturn().getResponse().getContentAsString();


        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // get the elastic ip and check the ip address
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals(UnitTestConfig.elasticIpv4Address1, response.getElasticip().getElasticIp());
        Assert.assertEquals(UnitTestConfig.elasticIpVersion1, response.getElasticip().getElasticIpVersion());
        Assert.assertEquals(UnitTestConfig.elasticIpName1, response.getElasticip().getName());
        Assert.assertEquals(UnitTestConfig.elasticIpDescription1, response.getElasticip().getDescription());
        Assert.assertEquals(elasticIpId, response.getElasticip().getId());


        // update the address of the elastic ip
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setElasticIp(UnitTestConfig.elasticIpv4Address2);
        putRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // this.showIPv4AllocationInfo();

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // this.showIPv4AllocationInfo();

        // check the elastic ip does not exist
        this.mockMvc.perform(get(deleteEipUri))
                .andDo(print())
                .andExpect(status().isNotFound());


        this.cleanRange();
    }

    @Test
    public void elasticIp_IpNotSpecified_create_delete() throws Exception {
        this.perpareRange();

        // create the elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // check the elastic ip exists
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // this.showIPv4AllocationInfo();

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // this.showIPv4AllocationInfo();

        // check the elastic ip does not exist
        this.mockMvc.perform(get(deleteEipUri))
                .andDo(print())
                .andExpect(status().isNotFound());

        this.cleanRange();
    }

    @Test
    public void elasticIp_Ipv6Specified_create_update_delete() throws Exception {
        this.perpareRange();

        // create the elastic ip (version IPv6)
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv6Address1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion2);
        postRequest.setName(UnitTestConfig.elasticIpName2);
        postRequest.setDescription(UnitTestConfig.elasticIpDescription2);

        this.showIPv6AllocationInfo();

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.elastic_ip")
                        .value(UnitTestConfig.elasticIpv6Address1))
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // get the elastic ip and check the ip address
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals(UnitTestConfig.elasticIpv6Address1, response.getElasticip().getElasticIp());
        Assert.assertEquals(UnitTestConfig.elasticIpVersion2, response.getElasticip().getElasticIpVersion());
        Assert.assertEquals(UnitTestConfig.elasticIpName2, response.getElasticip().getName());
        Assert.assertEquals(UnitTestConfig.elasticIpDescription2, response.getElasticip().getDescription());
        Assert.assertEquals(elasticIpId, response.getElasticip().getId());

        // update the address of the elastic ip
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setElasticIp(UnitTestConfig.elasticIpv6Address2);
        putRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion2);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // this.showIPv6AllocationInfo();

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // this.showIPv6AllocationInfo();

        // check the elastic ip does not exist
        this.mockMvc.perform(get(deleteEipUri))
                .andDo(print())
                .andExpect(status().isNotFound());

        this.cleanRange();
    }

    @Test
    public void elasticIp_Ipv6NotSpecified_create_delete() throws Exception {
        this.perpareRange();

        // create the elastic ip (version IPv6)
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion2);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.elastic_ip_version")
                        .value(UnitTestConfig.elasticIpVersion2))
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();
        String ipv6Address = response.getElasticip().getElasticIp();

        // get the elastic ip and check the ip address
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals(ipv6Address, response.getElasticip().getElasticIp());
        Assert.assertEquals(UnitTestConfig.elasticIpVersion2, response.getElasticip().getElasticIpVersion());
        Assert.assertEquals(elasticIpId, response.getElasticip().getId());

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // this.showIPv6AllocationInfo();

        // check the elastic ip does not exist
        this.mockMvc.perform(get(deleteEipUri))
                .andDo(print())
                .andExpect(status().isNotFound());

        this.cleanRange();
    }

    @Test
    public void elasticIpRange_ipv4Range_update_delete() throws Exception {
        this.perpareRange();

        // create an Ipv4 elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv4Address1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // try to delete the range
        String deleteRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(delete(deleteRangeUri))
                .andDo(print())
                .andExpect(status().isNotAcceptable());

        // try to update the allocation ranges to new ranges
        ElasticIpRange putRequest = new ElasticIpRange();

        List<ElasticIpRange.AllocationRange> allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart3,
                UnitTestConfig.elasticIpRangeEnd3));
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange1);

        ElasticIpRangeInfoWrapper rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        String updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isNotAcceptable());

        // add new range to allocation ranges
        putRequest = new ElasticIpRange();
        allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart1,
                UnitTestConfig.elasticIpRangeEnd1));
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart3,
                UnitTestConfig.elasticIpRangeEnd3));
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange1);

        rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        responseStr = this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ElasticIpRangeInfoWrapper rangeResponse = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
        List<ElasticIpRange.AllocationRange> resultRanges = rangeResponse.getElasticIpRange().getAllocationRanges();
        Assert.assertEquals(2, resultRanges.size());

        // create an new Ipv4 elastic ip using new ranges
        postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv4Address3);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);
        createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId2 = response.getElasticip().getId();

        // delete the new created elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId2;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // remove a range from allocation ranges
        putRequest = new ElasticIpRange();
        allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart1,
                UnitTestConfig.elasticIpRangeEnd1));
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange1);

        rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        responseStr = this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        rangeResponse = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
        resultRanges = rangeResponse.getElasticIpRange().getAllocationRanges();
        Assert.assertEquals(1, resultRanges.size());

        // delete the elastic ip
        deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIpRange_ipv6Range_update_delete() throws Exception {
        this.perpareRange();

        // create an Ipv6 elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv6Address1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion2);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // try to delete the range
        String deleteRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
        this.mockMvc.perform(delete(deleteRangeUri))
                .andDo(print())
                .andExpect(status().isNotAcceptable());

        // try to update the allocation ranges to new ranges
        ElasticIpRange putRequest = new ElasticIpRange();

        List<ElasticIpRange.AllocationRange> allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart4,
                UnitTestConfig.elasticIpRangeEnd4));
        putRequest.setIpVersion(UnitTestConfig.elasticIpRangeIpVersion2);
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange2);

        ElasticIpRangeInfoWrapper rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        String updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
        this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isNotAcceptable());

        // add new range to allocation ranges
        putRequest = new ElasticIpRange();
        allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart2,
                UnitTestConfig.elasticIpRangeEnd2));
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart4,
                UnitTestConfig.elasticIpRangeEnd4));
        putRequest.setIpVersion(UnitTestConfig.elasticIpRangeIpVersion2);
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange2);

        rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
        responseStr = this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ElasticIpRangeInfoWrapper rangeResponse = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
        List<ElasticIpRange.AllocationRange> resultRanges = rangeResponse.getElasticIpRange().getAllocationRanges();
        Assert.assertEquals(2, resultRanges.size());

        // create an Ipv6 elastic ip using new ranges
        postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIp(UnitTestConfig.elasticIpv6Address3);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion2);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);
        createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId2 = response.getElasticip().getId();

        // delete the new created elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId2;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        // remove a range from allocation ranges
        putRequest = new ElasticIpRange();
        allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart2,
                UnitTestConfig.elasticIpRangeEnd2));
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setIpVersion(UnitTestConfig.elasticIpRangeIpVersion2);
        putRequest.setId(UnitTestConfig.elasticIpRange2);

        rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange2;
        responseStr = this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        rangeResponse = mapper.readValue(responseStr, ElasticIpRangeInfoWrapper.class);
        resultRanges = rangeResponse.getElasticIpRange().getAllocationRanges();
        Assert.assertEquals(1, resultRanges.size());

        // delete the elastic ip
        deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIp_updateDns() throws Exception {
        this.perpareRange();

        // create the elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // get the elastic ip and check dns config is empty
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals("", response.getElasticip().getDnsDomain());
        Assert.assertEquals("", response.getElasticip().getDnsName());

        // update dns config of the elastic ip
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setDnsDomain(UnitTestConfig.elasticIpDnsDomain1);
        putRequest.setDnsName(UnitTestConfig.elasticIpDnsName1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk());

        // get the elastic ip and check dns config success
        getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_domain")
                        .value(UnitTestConfig.elasticIpDnsDomain1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_name")
                        .value(UnitTestConfig.elasticIpDnsName1))
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals(UnitTestConfig.elasticIpDnsDomain1, response.getElasticip().getDnsDomain());
        Assert.assertEquals(UnitTestConfig.elasticIpDnsName1, response.getElasticip().getDnsName());

        // update other config of the elastic ip and check dns config not changed
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setDescription(UnitTestConfig.elasticIpRangeDescription1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_domain")
                        .value(UnitTestConfig.elasticIpDnsDomain1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_name")
                        .value(UnitTestConfig.elasticIpDnsName1));

        // update dns config of the elastic ip to empty str
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setDnsDomain("");
        putRequest.setDnsName("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_domain").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.dns_name").value(""));

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIp_updateNameAndDescription() throws Exception {
        this.perpareRange();

        // create the elastic ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setElasticIpVersion(UnitTestConfig.elasticIpVersion1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";
        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // get the elastic ip and check name and description are empty
        String getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals("", response.getElasticip().getName());
        Assert.assertEquals("", response.getElasticip().getDescription());

        // update name and description of the elastic ip
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setName(UnitTestConfig.elasticIpName1);
        putRequest.setDescription(UnitTestConfig.elasticIpDescription1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk());

        // get the elastic ip and check name and descritpion config success
        getEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        responseStr = this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.name")
                        .value(UnitTestConfig.elasticIpName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.description")
                        .value(UnitTestConfig.elasticIpDescription1))
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        Assert.assertEquals(UnitTestConfig.elasticIpName1, response.getElasticip().getName());
        Assert.assertEquals(UnitTestConfig.elasticIpDescription1, response.getElasticip().getDescription());

        // update other config of the elastic ip and name and description not changed
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setDnsDomain(UnitTestConfig.elasticIpDnsDomain1);
        putRequest.setDnsName(UnitTestConfig.elasticIpDnsName1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.name")
                        .value(UnitTestConfig.elasticIpName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.description")
                        .value(UnitTestConfig.elasticIpDescription1));

        // update name and description of the elastic ip to empty str
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setName("");
        putRequest.setDescription("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.name").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.description").value(""));

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIpRange_updateNameAndDescription() throws Exception {
        this.perpareRange();

        // update other config and check name and description not changed
        ElasticIpRange putRequest = new ElasticIpRange();
        List<ElasticIpRange.AllocationRange> allocationRanges = new ArrayList<>();
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart1,
                UnitTestConfig.elasticIpRangeEnd1));
        allocationRanges.add(new ElasticIpRange.AllocationRange(
                UnitTestConfig.elasticIpRangeStart3,
                UnitTestConfig.elasticIpRangeEnd3));
        putRequest.setAllocationRanges(allocationRanges);
        putRequest.setId(UnitTestConfig.elasticIpRange1);

        ElasticIpRangeInfoWrapper rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        String updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.name")
                        .value(UnitTestConfig.elasticIpRangeName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.description")
                        .value(UnitTestConfig.elasticIpRangeDescription1));

        String getEipUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.name")
                        .value(UnitTestConfig.elasticIpRangeName1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.description")
                        .value(UnitTestConfig.elasticIpRangeDescription1));

        // update name and description of the elastic ip range to empty str
        putRequest = new ElasticIpRange();
        putRequest.setName("");
        putRequest.setDescription("");
        putRequest.setId(UnitTestConfig.elasticIpRange1);

        rangeRequestWraper = new ElasticIpRangeInfoWrapper(new ElasticIpRangeInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(rangeRequestWraper);
        updateEipRangeUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(put(updateEipRangeUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.name")
                        .value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.description")
                        .value(""));

        getEipUri = "/elasticip-ranges/" + UnitTestConfig.elasticIpRange1;
        this.mockMvc.perform(get(getEipUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.name")
                        .value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip-range.description")
                        .value(""));

        this.cleanRange();
    }

    @Test
    public void elasticIp_associateWithPort() throws Exception {
        this.perpareRange();

        // mock the port manager rest client
        PortEntity port = new PortEntity();
        port.setProjectId(UnitTestConfig.projectId1);
        port.setId(UnitTestConfig.elasticIpPort1);
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp();
        fixedIp.setIpAddress(UnitTestConfig.elasticIpPrivateIp1);
        fixedIp.setSubnetId(UnitTestConfig.getElasticIpPrivateIpSubnetId1);
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(fixedIp);
        port.setFixedIps(fixedIps);
        Mockito.when(portManagerRestClient.getPort(UnitTestConfig.projectId1, UnitTestConfig.elasticIpPort1))
                .thenReturn(new PortWebJson(port));

        // create the elastic ip associate with a port and not specify a private ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setPortId(UnitTestConfig.elasticIpPort1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .value(UnitTestConfig.elasticIpPrivateIp1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip_version")
                        .value(UnitTestConfig.elasticIpPrivateIpVersion1.toString()))
                .andReturn().getResponse().getContentAsString();

        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // disassociate elastic ip with the port
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // associate elastic ip with a port and specify a private ip
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId(UnitTestConfig.elasticIpPort1);
        putRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion1);
        putRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .value(UnitTestConfig.elasticIpPrivateIp1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip_version")
                        .value(UnitTestConfig.elasticIpPrivateIpVersion1.toString()));

        // disassociate elastic ip with the port
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIp_associateWithMultiplyIpsPort() throws Exception {
        this.perpareRange();

        // mock the port manager rest client
        PortEntity port = new PortEntity();
        port.setProjectId(UnitTestConfig.projectId1);

        port.setId(UnitTestConfig.elasticIpPort1);
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp();
        fixedIp.setIpAddress(UnitTestConfig.elasticIpPrivateIp1);
        fixedIp.setSubnetId(UnitTestConfig.getElasticIpPrivateIpSubnetId1);

        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp();
        fixedIp2.setIpAddress(UnitTestConfig.elasticIpPrivateIp2);
        fixedIp2.setSubnetId(UnitTestConfig.getElasticIpPrivateIpSubnetId2);

        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(fixedIp);
        fixedIps.add(fixedIp2);
        port.setFixedIps(fixedIps);
        Mockito.when(portManagerRestClient.getPort(UnitTestConfig.projectId1, UnitTestConfig.elasticIpPort1))
                .thenReturn(new PortWebJson(port));

        // create the elastic ip associate with a multiply fixed ips port and not specify a private ip
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setPortId(UnitTestConfig.elasticIpPort1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip_version")
                        .isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // disassociate elastic ip with the port
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // associate elastic ip with a port and specify a private ip not in the fixed ips of the port
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId(UnitTestConfig.elasticIpPort1);
        putRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion2);
        putRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp3);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // disassociate elastic ip with the port
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // associate elastic ip with a port and specify a private ip within the fixed ips of the port
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId(UnitTestConfig.elasticIpPort1);
        putRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion1);
        putRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp2);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .value(UnitTestConfig.elasticIpPrivateIp2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip_version")
                        .value(UnitTestConfig.elasticIpPrivateIpVersion1.toString()));

        // disassociate elastic ip with the port
        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Test
    public void elasticIp_associatePortPrivateIpConflict() throws Exception {
        this.perpareRange();

        // mock the port manager rest client
        PortEntity port = new PortEntity();
        port.setProjectId(UnitTestConfig.projectId1);

        port.setId(UnitTestConfig.elasticIpPort1);
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp();
        fixedIp.setIpAddress(UnitTestConfig.elasticIpPrivateIp1);
        fixedIp.setSubnetId(UnitTestConfig.getElasticIpPrivateIpSubnetId1);

        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp();
        fixedIp2.setIpAddress(UnitTestConfig.elasticIpPrivateIp2);
        fixedIp2.setSubnetId(UnitTestConfig.getElasticIpPrivateIpSubnetId2);

        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(fixedIp);
        fixedIps.add(fixedIp2);
        port.setFixedIps(fixedIps);
        Mockito.when(portManagerRestClient.getPort(UnitTestConfig.projectId1, UnitTestConfig.elasticIpPort1))
                .thenReturn(new PortWebJson(port));

        // create the elastic ip associate with a port
        ElasticIp postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setPortId(UnitTestConfig.elasticIpPort1);
        postRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion1);
        postRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp1);

        ElasticIpInfoWrapper requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        ObjectMapper mapper = new ObjectMapper();
        String requestStr =  mapper.writeValueAsString(requestWraper);
        String createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        String responseStr = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .value(UnitTestConfig.elasticIpPrivateIp1))
                .andReturn().getResponse().getContentAsString();

        ElasticIpInfoWrapper response = mapper.readValue(responseStr, ElasticIpInfoWrapper.class);
        String elasticIpId = response.getElasticip().getId();

        // create another elastic ip associate with the same port and same fixed ip
        postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setPortId(UnitTestConfig.elasticIpPort1);
        postRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion1);
        postRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp1);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);
        createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // create another elastic ip associate with the same port and different fixed ip
        postRequest = new ElasticIp();
        postRequest.setProjectId(UnitTestConfig.projectId1);
        postRequest.setPortId(UnitTestConfig.elasticIpPort1);
        postRequest.setPrivateIpVersion(UnitTestConfig.elasticIpPrivateIpVersion1);
        postRequest.setPrivateIp(UnitTestConfig.elasticIpPrivateIp2);

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(postRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);
        createEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips";

        String responseStr2 = this.mockMvc.perform(post(createEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(UnitTestConfig.elasticIpPort1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.private_ip")
                        .value(UnitTestConfig.elasticIpPrivateIp2))
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseStr2, ElasticIpInfoWrapper.class);
        String elasticIpId2 = response.getElasticip().getId();

        // disassociate the elastic ip with the port
        ElasticIp putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        String updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        putRequest = new ElasticIp();
        putRequest.setProjectId(UnitTestConfig.projectId1);
        putRequest.setId(elasticIpId2);
        putRequest.setPortId("");

        requestWraper = new ElasticIpInfoWrapper(new ElasticIpInfo(putRequest));
        mapper = new ObjectMapper();
        requestStr =  mapper.writeValueAsString(requestWraper);

        updateEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId2;
        this.mockMvc.perform(put(updateEipUri).contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.elasticip.port_id")
                        .value(""));

        // delete the elastic ip
        String deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        deleteEipUri = "/project/" + UnitTestConfig.projectId1 + "/elasticips/" + elasticIpId2;
        this.mockMvc.perform(delete(deleteEipUri))
                .andDo(print())
                .andExpect(status().isOk());

        this.cleanRange();
    }

    @Before
    public void before() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }
}
