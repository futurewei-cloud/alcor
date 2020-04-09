package com.futurewei.alcor.subnet.entity;

import lombok.Data;

@Data
public class MacStateJson {

    private MacState macState;

    public MacStateJson () {}

    public MacStateJson (MacState macState) {
        this.macState = macState;
    }

    public MacState getMacState() {
        return macState;
    }

    public void setMacState(MacState macState) {
        this.macState = macState;
    }
}
