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

public class SecurityGroupEntity extends CustomerResource {
    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("security_group_rules")
    private List<SecurityGroupRuleEntity> securityGroupRuleEntities;

    @JsonProperty("create_at")
    private String createAt;

    @JsonProperty("update_at")
    private String updateAt;

    public SecurityGroupEntity() {
    }

    public SecurityGroupEntity(String tenantId, List<SecurityGroupRuleEntity> securityGroupRuleEntities, String createAt, String updateAt) {
        this.tenantId = tenantId;
        this.securityGroupRuleEntities = securityGroupRuleEntities;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public SecurityGroupEntity(CustomerResource state, String tenantId, List<SecurityGroupRuleEntity> securityGroupRuleEntities, String createAt, String updateAt) {
        super(state);
        this.tenantId = tenantId;
        this.securityGroupRuleEntities = securityGroupRuleEntities;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public SecurityGroupEntity(String projectId, String id, String name, String description, String tenantId, List<SecurityGroupRuleEntity> securityGroupRuleEntities, String createAt, String updateAt) {
        super(projectId, id, name, description);
        this.tenantId = tenantId;
        this.securityGroupRuleEntities = securityGroupRuleEntities;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<SecurityGroupRuleEntity> getSecurityGroupRuleEntities() {
        return securityGroupRuleEntities;
    }

    public void setSecurityGroupRuleEntities(List<SecurityGroupRuleEntity> securityGroupRuleEntities) {
        this.securityGroupRuleEntities = securityGroupRuleEntities;
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

    @Override
    public String toString() {
        return "SecurityGroup{" +
                "tenantId='" + tenantId + '\'' +
                ", securityGroupRules=" + securityGroupRuleEntities +
                ", createAt='" + createAt + '\'' +
                ", updateAt='" + updateAt + '\'' +
                '}';
    }
}
