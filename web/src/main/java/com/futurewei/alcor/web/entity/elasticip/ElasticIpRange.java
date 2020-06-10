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

package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.Resource;

import java.math.BigInteger;
import java.util.*;

public class ElasticIpRange extends Resource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("allocation_ranges")
    private List<AllocationRange> allocationRanges;

    public ElasticIpRange() {
    }

    public ElasticIpRange(String id, String name, String description, Integer ipVersion,
                          List<AllocationRange> allocationRanges) {
        super(id);
        this.name = name;
        this.description = description;
        this.ipVersion = ipVersion;
        this.allocationRanges = allocationRanges;
    }

    public ElasticIpRange(ElasticIpRange elasticIpRange) {
        super(elasticIpRange.getId());
        this.name = elasticIpRange.getName();
        this.description = elasticIpRange.getDescription();
        this.ipVersion = elasticIpRange.getIpVersion();
        this.allocationRanges = new ArrayList<>();
        for (AllocationRange range: elasticIpRange.getAllocationRanges()) {
            allocationRanges.add(new AllocationRange(range));
        }
    }


    public class AllocationRange {

        @JsonProperty("start")
        private String start;

        @JsonProperty("end")
        private String end;

        public AllocationRange() {}

        public AllocationRange(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public AllocationRange(AllocationRange range) {
            this.start = range.getStart();
            this.end = range.getEnd();
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public boolean checkIfAddressInRange(String address) {
            BigInteger addressInt = new BigInteger(address);
            BigInteger startInt = new BigInteger(start);
            BigInteger endInt = new BigInteger(end);

            return addressInt.compareTo(endInt) <= 0  && addressInt.compareTo(startInt) >= 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public List<AllocationRange> getAllocationRanges() {
        return allocationRanges;
    }

    public void setAllocationRanges(List<AllocationRange> allocationRanges) {
        this.allocationRanges = allocationRanges;
    }

    public boolean checkIfAddressInRange(String address) {
        for (AllocationRange range: allocationRanges) {
            if (range.checkIfAddressInRange(address)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "ElasticIpRange{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ipVersion='" + ipVersion + '\'' +
                ", allocationRanges=" + allocationRanges +
                "} " + super.toString();
    }
}
