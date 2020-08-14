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

package com.futurewei.alcor.quota.config;

import org.junit.BeforeClass;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestEnvConfiguration {

    @MockBean
    private static DefaultQuota defaultQuota;

    @BeforeClass
    public static void initEnv() {
        Map<String, Integer> defaultQuotaMap = new HashMap<>();
        defaultQuotaMap.put("floating_ip", 50);
        defaultQuotaMap.put("network", 10);
        defaultQuotaMap.put("port", 50);
        defaultQuotaMap.put("rbac_policy", -1);
        defaultQuotaMap.put("router", 10);
        defaultQuotaMap.put("security_group", 10);
        defaultQuotaMap.put("security_group_rule", 100);
        defaultQuotaMap.put("subnet", 10);
        defaultQuotaMap.put("subnetpool", -1);

        when(defaultQuota.getDefaults()).thenReturn(defaultQuotaMap);
        when(defaultQuota.getDefaultsCopy()).thenReturn(new HashMap<>(defaultQuotaMap));
    }
}
