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
package com.futurewei.alcor.subnet.entity;

import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

@Data
public class SubnetProgramInfo {

    private SubnetEntity customerSubnetEntity;
    private VpcEntity customerVpcState;

    public SubnetProgramInfo(SubnetEntity customerSubnetEntity, VpcEntity customerVpcState) {
        this.customerSubnetEntity = customerSubnetEntity;
        this.customerVpcState = customerVpcState;
    }

    public SubnetEntity getCustomerSubnetEntity() {
        return customerSubnetEntity;
    }

    public void setCustomerSubnetEntity(SubnetEntity customerSubnetEntity) {
        this.customerSubnetEntity = customerSubnetEntity;
    }

    public VpcEntity getCustomerVpcState() {
        return customerVpcState;
    }

    public void setCustomerVpcState(VpcEntity customerVpcState) {
        this.customerVpcState = customerVpcState;
    }

}
