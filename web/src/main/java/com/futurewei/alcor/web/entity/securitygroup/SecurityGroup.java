/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
