package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VpcsWebJson {

    private ArrayList<VpcEntity> vpcs;

    public VpcsWebJson() {
    }

    public VpcsWebJson(List<VpcEntity> networks) {
        this.vpcs = new ArrayList<>(networks);
    }

    public ArrayList<VpcEntity> getVpcs() {
        return vpcs;
    }

    public void setVpcs(ArrayList<VpcEntity> vpcs) {
        this.vpcs = vpcs;
    }
}
