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
