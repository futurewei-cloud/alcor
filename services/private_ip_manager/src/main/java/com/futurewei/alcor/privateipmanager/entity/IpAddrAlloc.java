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

package com.futurewei.alcor.privateipmanager.entity;


public class IpAddrAlloc {
    private int ipVersion;
    private String subnetId;
    private String rangeId;
    private String ipAddr;
    private String state;

    public IpAddrAlloc() {
    }

    public IpAddrAlloc(int ipVersion, String subnetId, String rangeId, String ipAddr, String state) {
        this.ipVersion = ipVersion;
        this.subnetId = subnetId;
        this.rangeId = rangeId;
        this.ipAddr = ipAddr;
        this.state = state;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "IpAddrAlloc{" +
                "ipVersion=" + ipVersion +
                ", subnetId='" + subnetId + '\'' +
                ", rangeId='" + rangeId + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
