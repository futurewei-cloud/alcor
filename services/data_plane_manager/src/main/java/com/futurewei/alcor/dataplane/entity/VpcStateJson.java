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
package com.futurewei.alcor.dataplane.entity;

import lombok.Data;

import static com.futurewei.alcor.schema.Vpc.VpcState;

@Data
public class VpcStateJson {
    private VpcState vpc;

    public VpcState getVpc() {
        return vpc;
    }

    public void setVpc(VpcState vpc) {
        this.vpc = vpc;
    }

    public VpcStateJson() {

    }

    public VpcStateJson(VpcState vpcState) {
        this.vpc = vpcState;
    }
}
