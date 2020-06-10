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

public class SecurityGroupWebJson {
    @JsonProperty("security_group")
    private SecurityGroupEntity securityGroupEntity;

    public SecurityGroupWebJson() {
    }

    public SecurityGroupWebJson(SecurityGroupEntity securityGroupEntity) {
        this.securityGroupEntity = securityGroupEntity;
    }

    public SecurityGroupEntity getSecurityGroupEntity() {
        return securityGroupEntity;
    }

    public void setSecurityGroupEntity(SecurityGroupEntity securityGroupEntity) {
        this.securityGroupEntity = securityGroupEntity;
    }

    @Override
    public String toString() {
        return "SecurityGroupJson{" +
                "securityGroup=" + securityGroupEntity +
                '}';
    }
}
