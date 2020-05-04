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
package com.futurewei.alcor.subnet.entity;

import com.futurewei.alcor.web.entity.SubnetWebObject;
import lombok.Data;

@Data
public class SubnetProgramInfo {

    private SubnetWebObject customerSubnetWebObject;
    private VpcState customerVpcState;

    public SubnetProgramInfo(SubnetWebObject customerSubnetWebObject, VpcState customerVpcState) {
        this.customerSubnetWebObject = customerSubnetWebObject;
        this.customerVpcState = customerVpcState;
    }

    public SubnetWebObject getCustomerSubnetWebObject() {
        return customerSubnetWebObject;
    }

    public void setCustomerSubnetWebObject(SubnetWebObject customerSubnetWebObject) {
        this.customerSubnetWebObject = customerSubnetWebObject;
    }

    public VpcState getCustomerVpcState() {
        return customerVpcState;
    }

    public void setCustomerVpcState(VpcState customerVpcState) {
        this.customerVpcState = customerVpcState;
    }

}
