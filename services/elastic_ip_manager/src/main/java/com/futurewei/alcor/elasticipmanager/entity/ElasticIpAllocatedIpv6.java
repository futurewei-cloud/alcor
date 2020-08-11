<<<<<<< HEAD
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
=======
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
>>>>>>> new_master
}