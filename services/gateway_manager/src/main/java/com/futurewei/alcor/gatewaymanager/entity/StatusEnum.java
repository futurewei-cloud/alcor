package com.futurewei.alcor.gatewaymanager.entity;

public enum StatusEnum {

    PENDING("PENDING"),
    READY("READY"),
    FAILED("FAILED"),
    AVAILABLE("available");

    private final String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
