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

package com.futurewei.alcor.elasticipmanager.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElasticIpAllocatedIpv4 {

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("tail_id")
    private int indexId;

    @JsonProperty("allocated_ipv4_set")
    private Set<Long> allocatedIps;

    @JsonProperty("available_ipv4_set")
    private Set<Long> availableIps;

    public ElasticIpAllocatedIpv4(String rangeId, int indexId) {
        this.rangeId = rangeId;
        this.indexId = indexId;
        this.allocatedIps = new HashSet<>();
        this.availableIps = new HashSet<>();
    }

    public ElasticIpAllocatedIpv4(String rangeId, int indexId, Set<Long> allocatedIps, Set<Long> availableIps) {
        this.rangeId = rangeId;
        this.indexId = indexId;
        this.allocatedIps = allocatedIps;
        this.availableIps = availableIps;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public int getIndexId() {
        return indexId;
    }

    public void setIndexId(int indexId) {
        this.indexId = indexId;
    }

    public Set<Long> getAllocatedIps() {
        return allocatedIps;
    }

    public void setAllocatedIps(Set<Long> allocatedIps) {
        this.allocatedIps = allocatedIps;
    }

    public Set<Long> getAvailableIps() {
        return availableIps;
    }

    public void setAvailableIps(Set<Long> availableIps) {
        this.availableIps = availableIps;
    }

    @Override
    public String toString() {
        return "ElasticIpAllocatedIpv4{" +
                "rangeId='" + rangeId + '\'' +
                ", indexId=" + indexId +
                ", allocatedIps=" + allocatedIps +
                ", availableIps=" + availableIps +
                '}';
    }
}