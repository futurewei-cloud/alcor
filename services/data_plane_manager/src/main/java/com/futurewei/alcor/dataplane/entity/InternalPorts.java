/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.dataplane.entity;

import java.util.List;

public class InternalPorts {
    private String subnetId;
    private String securityGroupId;
    private List<String> portIds;

    public InternalPorts() { };

    public InternalPorts(String subnetId, String securityGroupId, List<String> portIds) {
        this.subnetId = subnetId;
        this.securityGroupId = securityGroupId;
        this.portIds = portIds;
    }

    public void setSubnetId(String subnetId) { this.subnetId = subnetId; }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public void setPortIds(List<String> portIds) {
        this.portIds = portIds;
    }

    public String getSubnetId() {
        return this.subnetId;
    }

    public String getSecurityGroupId() {
        return this.securityGroupId;
    }

    public List<String>getPortIds() {
        return this.portIds;
    }
}
