package com.futurewei.alcor.web.entity.elasticip;

public enum ElasticIpState {
    ACTIVATED("activated"),
    DEACTIVATED("deactivated");

    private String state;

    ElasticIpState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ElasticIpState{" +
                "state='" + state + '\'' +
                "} " + super.toString();
    }
}
