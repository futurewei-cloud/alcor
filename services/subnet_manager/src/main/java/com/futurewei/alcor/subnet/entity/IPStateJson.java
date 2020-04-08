package com.futurewei.alcor.subnet.entity;

import lombok.Data;

@Data
public class IPStateJson {

    private IPState ipState;

    public IPStateJson () {}

    public IPStateJson (IPState ipState) {
        this.ipState = ipState;
    }

    public IPState getIpState() {
        return ipState;
    }

    public void setIpState(IPState ipState) {
        this.ipState = ipState;
    }
}
