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
    private Integer ipVersion;

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
        if (elasticIpRange.getAllocationRanges() != null) {
            for (AllocationRange range: elasticIpRange.getAllocationRanges()) {
                allocationRanges.add(new AllocationRange(range));
            }
        }
    }


    public static class AllocationRange {

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

    public void setIpVersion(Integer ipVersion) {
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
