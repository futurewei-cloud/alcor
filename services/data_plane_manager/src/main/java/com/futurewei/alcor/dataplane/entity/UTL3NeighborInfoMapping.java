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

import java.util.List;

public class UTL3NeighborInfoMapping {

    private String subnetId;

    // list of IPs in this subnet
    private List<String> IPsInSubnet;

    public UTL3NeighborInfoMapping() {
    }

    public UTL3NeighborInfoMapping(String subnetId, List<String> IPsInSubnet) {
        this.subnetId = subnetId;
        this.IPsInSubnet = IPsInSubnet;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public List<String> getIPsInSubnet() {
        return IPsInSubnet;
    }

    public void setIPsInSubnet(List<String> IPsInSubnet) {
        this.IPsInSubnet = IPsInSubnet;
    }
}
