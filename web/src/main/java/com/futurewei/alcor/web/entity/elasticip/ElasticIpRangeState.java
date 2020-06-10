package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ElasticIpRangeState extends ElasticIpRange {

    @JsonProperty("used_ip_count")
    private Long used_ip_count;

    public ElasticIpRangeState() {
    }

    public ElasticIpRangeState(ElasticIpRange elasticIpRange) {
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
        return "ElasticipRangeState{" +
                "used_ip_count='" + used_ip_count + '\'' +
                "} " + super.toString();
    }
}
