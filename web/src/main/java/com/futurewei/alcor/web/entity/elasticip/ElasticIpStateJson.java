package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticIpStateJson {

    @JsonProperty("elastic_ip")
    private ElasticIpState elasticip;

    public ElasticIpStateJson() {
    }

    public ElasticIpStateJson(ElasticIpState elasticipState) {
        this.elasticip = elasticipState;
    }

    public ElasticIpState getElasticip() {
        return this.elasticip;
    }

    public void setElasticip(ElasticIpState elasticipState) {
        this.elasticip = elasticipState;
    }

}
