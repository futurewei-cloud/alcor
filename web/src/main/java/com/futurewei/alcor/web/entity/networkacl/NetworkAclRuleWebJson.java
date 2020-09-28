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

public class NetworkAclRuleWebJson {
    @JsonProperty("network_acl_rule")
    private NetworkAclRuleEntity networkAclRuleEntity;

    public NetworkAclRuleWebJson() {

    }

    public NetworkAclRuleWebJson(NetworkAclRuleEntity networkAclRuleEntity) {
        this.networkAclRuleEntity = networkAclRuleEntity;
    }

    public NetworkAclRuleEntity getNetworkAclRuleEntity() {
        return networkAclRuleEntity;
    }

    public void setNetworkAclRuleEntity(NetworkAclRuleEntity networkAclRuleEntity) {
        this.networkAclRuleEntity = networkAclRuleEntity;
    }
}
