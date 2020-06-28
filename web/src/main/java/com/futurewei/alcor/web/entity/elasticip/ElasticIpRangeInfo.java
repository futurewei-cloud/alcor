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
