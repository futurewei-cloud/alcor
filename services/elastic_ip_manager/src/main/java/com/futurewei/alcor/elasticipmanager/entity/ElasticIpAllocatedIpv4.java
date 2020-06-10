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

package com.futurewei.alcor.elasticipmanager.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public class ElasticIpAllocatedIpv4 {

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("tail_id")
    private int tailId;

    @JsonProperty("allocated_ipv4_set")
    private Set<Long> allocatedIps;

    public ElasticIpAllocatedIpv4(String rangeId, int tailId) {
        this.rangeId = rangeId;
        this.tailId = tailId;
        this.allocatedIps = new HashSet<>();
    }

    public ElasticIpAllocatedIpv4(String rangeId, int tailId, Set<Long> allocatedIps) {
        this.rangeId = rangeId;
        this.tailId = tailId;
        this.allocatedIps = allocatedIps;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public int getTailId() {
        return tailId;
    }

    public void setTailId(int tailId) {
        this.tailId = tailId;
    }

    public Set<Long> getAllocatedIps() {
        return allocatedIps;
    }

    public void setAllocatedIps(Set<Long> allocatedIps) {
        this.allocatedIps = allocatedIps;
    }

    @Override
    public String toString() {
        return "ElasticIpAllocatedIpv4{" +
                "rangeId='" + rangeId + '\'' +
                ", tailId=" + tailId +
                ", allocatedIps=" + allocatedIps +
                '}';
    }
}