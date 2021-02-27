package com.futurewei.alcor.portmanager.entity;

public enum PortStatusEnum {

    CREATED("CREATED"),
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    PENDING("PENDING");

    private String status;

    PortStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
