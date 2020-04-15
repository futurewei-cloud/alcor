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

package com.futurewei.alcor.ipmanager.entity;


public class Ipv4AddrRange {
    private String subnetId;
    private String firstAddr;
    private String lastAddr;

    public Ipv4AddrRange() {
    }

    public Ipv4AddrRange(String subnetId, String firstAddr, String lastAddr) {
        this.subnetId = subnetId;
        this.firstAddr = firstAddr;
        this.lastAddr = lastAddr;
    }

    public Ipv4AddrRange(Ipv4AddrRange ipv4AddrRange) {
        this(ipv4AddrRange.getSubnetId(), ipv4AddrRange.getFirstAddr(), ipv4AddrRange.getLastAddr());
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstAddr() {
        return firstAddr;
    }

    public void setFirstAddr(String firstAddr) {
        this.firstAddr = firstAddr;
    }

    public String getLastAddr() {
        return lastAddr;
    }

    public void setLastAddr(String lastAddr) {
        this.lastAddr = lastAddr;
    }
}
