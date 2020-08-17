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

public class ElasticIpRangeInfoWrapper {

    @JsonProperty("elasticip-range")
    private ElasticIpRangeInfo elasticIpRange;

    public ElasticIpRangeInfoWrapper() {
    }

    public ElasticIpRangeInfoWrapper(ElasticIpRangeInfo elasticipRangeInfo) {
        this.elasticIpRange = elasticipRangeInfo;
    }

    public ElasticIpRangeInfo getElasticIpRange() {
        return this.elasticIpRange;
    }

    public void setElasticIpRange(ElasticIpRangeInfo elasticipRangeInfo) {
        this.elasticIpRange = elasticipRangeInfo;
    }
}
