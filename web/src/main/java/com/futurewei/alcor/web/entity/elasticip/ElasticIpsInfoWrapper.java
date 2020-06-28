package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticIpsInfoWrapper {

    @JsonProperty("elastic_ips")
    private List<ElasticIpInfo> elasticips;

    public ElasticIpsInfoWrapper() {
    }

    public ElasticIpsInfoWrapper(List<ElasticIpInfo> elasticipsState) {
        this.elasticips = elasticipsState;
    }

    public List<ElasticIpInfo> getElasticips() {
        return this.elasticips;
    }

    public void setElasticips(List<ElasticIpInfo> elasticipsState) {
        this.elasticips = elasticipsState;
    }

}
