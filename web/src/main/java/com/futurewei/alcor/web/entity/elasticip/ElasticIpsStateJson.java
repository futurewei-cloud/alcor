package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ElasticIpsStateJson {

    @JsonProperty("elastic_ips")
    private List<ElasticIpState> elasticips;

    public ElasticIpsStateJson() {
    }

    public ElasticIpsStateJson(List<ElasticIpState> elasticipsState) {
        this.elasticips = elasticipsState;
    }

    public List<ElasticIpState> getElasticips() {
        return this.elasticips;
    }

    public void setElasticips(List<ElasticIpState> elasticipsState) {
        this.elasticips = elasticipsState;
    }

}
