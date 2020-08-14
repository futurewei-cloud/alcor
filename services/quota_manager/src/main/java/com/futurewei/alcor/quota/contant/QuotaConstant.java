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

package com.futurewei.alcor.quota.contant;

public class QuotaConstant {

    public static final String FLOATING_IP = "floating_ip";
    public static final String NETWORK = "network";
    public static final String PORT = "port";
    public static final String RBAC_POLICY = "rbac_policy";
    public static final String ROUTER = "router";
    public static final String SECURITY_GROUP = "security_group";
    public static final String SECURITY_GROUP_RULE = "security_group_rule";
    public static final String SUBNET = "subnet";
    public static final String SUBNETPOOL = "subnetpool";


    public static final String[] RESOURCES = new String[] {FLOATING_IP, NETWORK, PORT, RBAC_POLICY,
            ROUTER, SECURITY_GROUP, SECURITY_GROUP_RULE, SUBNET, SUBNETPOOL};
}
