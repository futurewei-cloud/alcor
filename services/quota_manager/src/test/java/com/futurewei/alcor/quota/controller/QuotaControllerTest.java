/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.quota.controller;

import com.futurewei.alcor.quota.config.DefaultQuota;
import com.futurewei.alcor.quota.config.TestEnvConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class QuotaControllerTest extends TestEnvConfiguration {
    private static final String projectId = "3d53801c-32ce-4e97-9572-bb966f4de79c";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultQuota defaultQuota;

    @Test
    public void getAllQuotasTest() {

    }

    @Test
    public void getAllQuotas_EmptyTest() {

    }

    @Test
    public void getQuotaTest() {

    }

    @Test
    public void getQuota_EmptyTest() {

    }

    @Test
    public void updateQuotaTest() {

    }

    @Test
    public void updateQuota_ExistTest() {

    }

    @Test
    public void deleteQuotaTest() {

    }

    @Test
    public void deleteQuota_EmptyTest() {

    }

    @Test
    public void getDefaultQuotaForProjectTest() throws Exception {
        String getDefaultUrl = "/project/" + projectId + "/quotas/" + projectId + "/default";
        mockMvc.perform(get(getDefaultUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(defaultQuota.getDefaults().get("network")));
    }

    @Test
    public void getProjectQuotaDetailTest() {

    }

    @Test
    public void getProjectQuotaDetail_EmptyTest() {

    }

    @Test
    public void allocateQuotaTest() {

    }

    @Test
    public void allocateQuota_EmptyTest() {

    }

    @Test
    public void allocateQuota_OverLimitTest() {

    }

    @Test
    public void allocateQuota_NoApplyBodyTest() {

    }

    @Test
    public void cancelQuotaTest() {

    }

    @Test
    public void cancelQuota_NoApplyTest() {

    }
}
