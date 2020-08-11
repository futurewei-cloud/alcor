package com.futurewei.alcor.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Resource implements Serializable {
    @JsonProperty("id")
    private String id;

    public Resource() {
    }

    public Resource(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
