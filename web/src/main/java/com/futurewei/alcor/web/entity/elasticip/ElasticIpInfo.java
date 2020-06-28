package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ElasticIpInfo extends ElasticIp {

    @JsonProperty("state")
    private String state;

    public ElasticIpInfo() {
    }

    public ElasticIpInfo(ElasticIp eip) {
        super(eip);
        if (eip.getPortId() != null) {
            this.state = ElasticIpState.ACTIVATED.getState();
        } else {
            this.state = ElasticIpState.DEACTIVATED.getState();
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ElasticIpInfo{" +
                "state='" + state + '\'' +
                "} " + super.toString();
    }
}
