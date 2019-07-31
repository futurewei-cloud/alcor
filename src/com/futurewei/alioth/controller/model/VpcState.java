package com.futurewei.alioth.controller.model;

import lombok.Data;

@Data
public class VpcState extends CustomerResource {

    private String cidr;

    public VpcState() {}

    public VpcState(String projectId, String id, String name, String cidr){
        this(projectId, id, name, cidr,null);
    }

    public VpcState(VpcState state){
        this(state.getProjectId(), state.getId(), state.getName(), state.getCidr(), state.getDescription());
    }

    public VpcState(String projectId, String id, String name, String cidr, String description){

        super(projectId, id, name, description);
        this.cidr = cidr;
    }

}


