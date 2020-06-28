package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticIpInfoWrapper {

    @JsonProperty("elastic_ip")
    private ElasticIpInfo elasticip;

    public ElasticIpInfoWrapper() {
    }

    public ElasticIpInfoWrapper(ElasticIpInfo elasticipInfo) {
        this.elasticip = elasticipInfo;
    }

    public ElasticIpInfo getElasticip() {
        return this.elasticip;
    }

    public void setElasticip(ElasticIpInfo elasticipInfo) {
        this.elasticip = elasticipInfo;
    }

}
