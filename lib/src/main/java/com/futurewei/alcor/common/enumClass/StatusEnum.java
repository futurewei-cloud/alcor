package com.futurewei.alcor.common.enumClass;

public enum StatusEnum {

    PENDING("pending"),
    READY("ready"),
    FAILED("failed"),
    AVAILABLE("available"),
    NOTAVAILABLE("not-available");

    private final String status;

    StatusEnum(String env) {
        this.status = env;
    }

    public String getStatus () {
        return status;
    }
}
