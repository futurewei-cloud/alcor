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

package com.futurewei.vpcmanager.entity;

import lombok.Data;

@Data
public class VpcState extends CustomerResource {

    private String cidr;

    public VpcState() {
    }

    public VpcState(String projectId, String id, String name, String cidr) {
        this(projectId, id, name, cidr, null);
    }

    public VpcState(VpcState state) {
        this(state.getProjectId(), state.getId(), state.getName(), state.getCidr(), state.getDescription());
    }

    public VpcState(String projectId, String id, String name, String cidr, String description) {

        super(projectId, id, name, description);
        this.cidr = cidr;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }
}


