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
package com.futurewei.alcor.dataplane.utils.entity;

public class UTSubnetInfo {

    private String subnetId;

    private String subnetCidr;

    private String subnetGatewayIP;

    private String subnetName;

    private Long tunnelId;

    public UTSubnetInfo() {

    }

    public UTSubnetInfo(String subnetId, String subnetCidr, String subnetGatewayIP, String subnetName, Long tunnelId) {
        this.subnetId = subnetId;
        this.subnetCidr = subnetCidr;
        this.subnetGatewayIP = subnetGatewayIP;
        this.subnetName = subnetName;
        this.tunnelId = tunnelId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getSubnetGatewayIP() {
        return subnetGatewayIP;
    }

    public void setSubnetGatewayIP(String subnetGatewayIP) {
        this.subnetGatewayIP = subnetGatewayIP;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }
}
