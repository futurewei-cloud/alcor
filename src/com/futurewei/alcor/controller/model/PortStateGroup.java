package com.futurewei.alcor.controller.model;

import lombok.Data;

import java.util.List;

@Data
public class PortStateGroup {

    private List<PortState> portStates;

    public PortStateGroup() {

    }

    public PortState getPortState(int index) {
        return this.getPortStates().get(index);
    }
}
