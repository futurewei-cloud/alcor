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

package com.futurewei.alcor.web.entity.quota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class QuotaDetailEntity extends CustomerResource {

    @JsonProperty("rbac_policy")
    private QuotaResourceUsage rbacPolicy;

    @JsonProperty("subnetpool")
    private QuotaResourceUsage subnetPool;

    @JsonProperty("security_group_rule")
    private QuotaResourceUsage securityGroupRule;

    @JsonProperty("security_group")
    private QuotaResourceUsage securityGroup;

    private QuotaResourceUsage subnet;

    private QuotaResourceUsage port;

    private QuotaResourceUsage network;

}
