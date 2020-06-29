package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticIpRangesInfoWrapper {

    @JsonProperty("elasticip-ranges")
    private List<ElasticIpRangeInfo> elasticipRanges;

    public ElasticIpRangesInfoWrapper(List<ElasticIpRangeInfo> elasticipRanges) {
        this.elasticipRanges = elasticipRanges;
    }

    public List<ElasticIpRangeInfo> getElasticipRanges() {
        return elasticipRanges;
    }

    public void setElasticipRanges(List<ElasticIpRangeInfo> elasticipRanges) {
        this.elasticipRanges = elasticipRanges;
    }
}
