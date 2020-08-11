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

public class PortBindingSecurityGroup {
    @JsonProperty("id")
    private String id;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("security_group_id")
    private String securityGroupId;

    public PortBindingSecurityGroup() {
    }

    public PortBindingSecurityGroup(String portId, String securityGroupId) {
        this.portId = portId;
        this.securityGroupId = securityGroupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }
}
