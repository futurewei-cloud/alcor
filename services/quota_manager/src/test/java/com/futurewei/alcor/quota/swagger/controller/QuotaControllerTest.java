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

package com.futurewei.alcor.quota.swagger.controller;

import com.futurewei.alcor.quota.swagger.config.TestEnvConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@SpringBootTest
@AutoConfigureMockMvc
public class QuotaControllerTest extends TestEnvConfiguration {

    @Autowired
    private MockMvc mockMvc;

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
    public void getDefaultQuotaForProject_Test() {

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
