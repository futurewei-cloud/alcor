package com.futurewei.alioth.controller.model;

import lombok.Data;

@Data
public class VpcState {

    private String project_id;
    private String id;
    private String name;
    private String cidr;

    VpcState() {}

    VpcState(String project_id, String id, String name, String cidr){
        this.project_id = project_id;
        this.id = id;
        this.name = name;
        this.cidr = cidr;
    }

}


