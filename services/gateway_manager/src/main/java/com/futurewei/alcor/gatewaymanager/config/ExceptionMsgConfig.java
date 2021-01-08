package com.futurewei.alcor.gatewaymanager.config;

public enum ExceptionMsgConfig {

    GATEWAY_ENTITY_NOT_FOUND("GatewayEntity could not be found"),
    ATTACHMENT_NOT_FOUND("Attachment could not be found"),
    GATEWAY_NOT_ASSOCIATED_ATTACHMENT("GatewayEntity not be associated the attachment");

    private String msg;

    ExceptionMsgConfig(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
