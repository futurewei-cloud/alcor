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


public class ElasticIpAllocatedIpv6 {

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("allocated_ipv6")
    private String allocatedIpv6;

    public ElasticIpAllocatedIpv6(String rangeId, String allocatedIpv6) {
        this.rangeId = rangeId;
        this.allocatedIpv6 = allocatedIpv6;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getAllocatedIpv6() {
        return allocatedIpv6;
    }

    public void setAllocatedIpv6(String allocatedIpv6) {
        this.allocatedIpv6 = allocatedIpv6;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ElasticIpAllocated{" +
                "rangeId='" + rangeId + '\'' +
                ", ipv6Addr='" + allocatedIpv6 + '\'' +
                '}';
    }
}