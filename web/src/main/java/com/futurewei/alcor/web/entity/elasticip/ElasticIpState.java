package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ElasticIpState extends ElasticIp {

    @JsonProperty("state")
    private String state;

    public ElasticIpState() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ElasticipState{" +
                "state='" + state + '\'' +
                "} " + super.toString();
    }
}
