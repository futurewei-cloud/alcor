package com.futurewei.alioth.controller.model;

import lombok.Data;

@Data
public class VpcState {

    private String projectId;
    private String id;
    private String name;
    private String cidr;
    private String description;

    public VpcState() {}

    public VpcState(String projectId, String id, String name, String cidr){
        this(projectId, id, name, cidr,null);
    }

    public VpcState(VpcState state){
        this(state.projectId, state.id, state.name, state.cidr, state.description);
    }

    public VpcState(String projectId, String id, String name, String cidr, String description){
        this.projectId = projectId;
        this.id = id;
        this.name = name;
        this.cidr = cidr;
        this.description = description;
    }

}


