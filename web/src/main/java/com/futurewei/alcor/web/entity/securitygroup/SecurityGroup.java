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
package com.futurewei.alcor.web.entity.securitygroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

import java.util.List;

public class SecurityGroup extends CustomerResource {
    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("security_group_rules")
    private List<SecurityGroupRule> securityGroupRules;

    @JsonProperty("create_at")
    private String createAt;

    @JsonProperty("update_at")
    private String updateAt;

    public SecurityGroup() {
    }

    public SecurityGroup(String tenantId, List<SecurityGroupRule> securityGroupRules, String createAt, String updateAt) {
        this.tenantId = tenantId;
        this.securityGroupRules = securityGroupRules;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public SecurityGroup(CustomerResource state, String tenantId, List<SecurityGroupRule> securityGroupRules, String createAt, String updateAt) {
        super(state);
        this.tenantId = tenantId;
        this.securityGroupRules = securityGroupRules;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public SecurityGroup(String projectId, String id, String name, String description, String tenantId, List<SecurityGroupRule> securityGroupRules, String createAt, String updateAt) {
        super(projectId, id, name, description);
        this.tenantId = tenantId;
        this.securityGroupRules = securityGroupRules;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<SecurityGroupRule> getSecurityGroupRules() {
        return securityGroupRules;
    }

    public void setSecurityGroupRules(List<SecurityGroupRule> securityGroupRules) {
        this.securityGroupRules = securityGroupRules;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

//    @Override
//    public String toString() {
//        return "SecurityGroup{" +
//                "tenantId='" + tenantId + '\'' +
//                ", securityGroupRules=" + securityGroupRules +
//                ", createAt='" + createAt + '\'' +
//                ", updateAt='" + updateAt + '\'' +
//                '}';
//    }
}
