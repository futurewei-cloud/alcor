package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticIpRangeStateJson {

    @JsonProperty("elastic_ip_range")
    private ElasticIpRangeState elasticIpRange;

    public ElasticIpRangeStateJson() {
    }

    public ElasticIpRangeStateJson(ElasticIpRangeState elasticipRangeState) {
        this.elasticIpRange = elasticipRangeState;
    }

    public ElasticIpRangeState getElasticIpRange() {
        return this.elasticIpRange;
    }

    public void setElasticIpRange(ElasticIpRangeState elasticipRangeState) {
        this.elasticIpRange = elasticipRangeState;
    }
}
