/*
Copyright 2020 The Alcor Authors.

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


public class ElasticIpRangeInfo extends ElasticIpRange {

    @JsonProperty("used_ip_count")
    private Long used_ip_count;

    public ElasticIpRangeInfo() {
    }

    public ElasticIpRangeInfo(ElasticIpRange elasticIpRange) {
        super(elasticIpRange);
        used_ip_count = 0L;
    }

    public Long getUsed_ip_count() {
        return used_ip_count;
    }

    public void setUsed_ip_count(Long used_ip_count) {
        this.used_ip_count = used_ip_count;
    }

    @Override
    public String toString() {
        return "ElasticIpRangeInfo{" +
                "used_ip_count='" + used_ip_count + '\'' +
                "} " + super.toString();
    }
}
