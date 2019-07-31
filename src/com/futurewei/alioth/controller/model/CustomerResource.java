package com.futurewei.alioth.controller.model;

import lombok.Data;

@Data
public class CustomerResource {
    private String projectId;
    private String id;
    private String name;
    private String description;

    public CustomerResource(){

    }

    public CustomerResource(CustomerResource state){
        this(state.projectId, state.id, state.name, state.description);
    }

    public CustomerResource(String projectId, String id, String name, String description){
        this.projectId = projectId;
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
