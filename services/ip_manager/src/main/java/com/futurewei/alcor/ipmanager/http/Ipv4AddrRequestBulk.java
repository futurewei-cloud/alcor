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

package com.futurewei.alcor.ipmanager.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Ipv4AddrRequestBulk {
    @JsonProperty("ipv4_addr_requests")
    private List<Ipv4AddrRequest> ipv4AddrRequests;

    public Ipv4AddrRequestBulk() {
    }

    public Ipv4AddrRequestBulk(List<Ipv4AddrRequest> ipv4AddrRequests) {
        this.ipv4AddrRequests = ipv4AddrRequests;
    }

    public List<Ipv4AddrRequest> getIpv4AddrRequests() {
        return ipv4AddrRequests;
    }

    public void setIpv4AddrRequests(List<Ipv4AddrRequest> ipv4AddrRequests) {
        this.ipv4AddrRequests = ipv4AddrRequests;
    }
}
