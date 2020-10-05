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
package com.futurewei.alcor.web.entity.networkacl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

import java.util.List;

public class NetworkAclEntity extends CustomerResource {
    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("associated_subnets")
    private List<String> associatedSubnets;

    @JsonProperty("network_acl_rules")
    private List<NetworkAclRuleEntity> networkAclRuleEntities;

    @JsonProperty("is_default")
    private String isDefault;

    public NetworkAclEntity() {
    }

    public NetworkAclEntity(String vpcId, List<String> associatedSubnets, List<NetworkAclRuleEntity> networkAclRuleEntities, String isDefault) {
        this.vpcId = vpcId;
        this.associatedSubnets = associatedSubnets;
        this.networkAclRuleEntities = networkAclRuleEntities;
        this.isDefault = isDefault;
    }

    public NetworkAclEntity(CustomerResource state, String vpcId, List<String> associatedSubnets, List<NetworkAclRuleEntity> networkAclRuleEntities, String isDefault) {
        super(state);
        this.vpcId = vpcId;
        this.associatedSubnets = associatedSubnets;
        this.networkAclRuleEntities = networkAclRuleEntities;
        this.isDefault = isDefault;
    }

    public NetworkAclEntity(String projectId, String id, String name, String description, String vpcId, List<String> associatedSubnets, List<NetworkAclRuleEntity> networkAclRuleEntities, String isDefault) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.associatedSubnets = associatedSubnets;
        this.networkAclRuleEntities = networkAclRuleEntities;
        this.isDefault = isDefault;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public List<String> getAssociatedSubnets() {
        return associatedSubnets;
    }

    public void setAssociatedSubnets(List<String> associatedSubnets) {
        this.associatedSubnets = associatedSubnets;
    }

    public List<NetworkAclRuleEntity> getNetworkAclRuleEntities() {
        return networkAclRuleEntities;
    }

    public void setNetworkAclRuleEntities(List<NetworkAclRuleEntity> networkAclRuleEntities) {
        this.networkAclRuleEntities = networkAclRuleEntities;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}
