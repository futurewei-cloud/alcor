package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VpcsWebJson {

    private ArrayList<VpcWebResponseObject> vpcs;

    public VpcsWebJson() {
    }

    public VpcsWebJson(List<VpcWebResponseObject> vpcs) {
        this.vpcs = new ArrayList<>(vpcs);
    }
}
