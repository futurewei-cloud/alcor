package com.futurewei.alcor.gatewaymanager.config;

public enum ExceptionMsgConfig {

    GATEWAY_ENTITY_NOT_FOUND("GatewayEntity could not be found"),
    ATTACHMENT_NOT_FOUND("Attachment could not be found"),
    GATEWAY_NOT_ASSOCIATED_ATTACHMENT("GatewayEntity not be associated the attachment"),
    VPCINFO_PARAMETER_ILLEGAL("VpcInfo parameters illegal,may be have null parameter or other situation"),
    RESOURCE_ID_IS_NULL("GatewayInfo's resource_id is null"),
    GATEWAYS_IS_NULL("GatewayInfo's gateways is null or empty"),
    GATEWAY_TYPE_OR_STATUS_IS_NULL("GatewayEntity's type or status is null"),
    GATEWAYINFO_NOT_FOUND("GatewayInfo could not be found"),
    ROLLBACK_FAILED("rollback failed");

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
