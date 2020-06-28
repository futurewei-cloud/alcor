package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticIpRangeInfoWrapper {

    @JsonProperty("elastic_ip_range")
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
