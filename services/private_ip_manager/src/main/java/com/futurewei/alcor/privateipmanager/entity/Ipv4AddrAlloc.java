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


public class Ipv4AddrAlloc {
    private String ipv4Addr;
    private String state;
    private String subnetId;

    public Ipv4AddrAlloc() {
    }

    public Ipv4AddrAlloc(String ipv4Addr, String state, String subnetId) {
        this.ipv4Addr = ipv4Addr;
        this.state = state;
        this.subnetId = subnetId;
    }

    public Ipv4AddrAlloc(Ipv4AddrAlloc ipv4AddrAlloc) {
        this(ipv4AddrAlloc.getIpv4Addr(), ipv4AddrAlloc.getState(), ipv4AddrAlloc.getSubnetId());
    }

    public String getIpv4Addr() {
        return ipv4Addr;
    }

    public void setIpv4Addr(String ipv4Addr) {
        this.ipv4Addr = ipv4Addr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
}
