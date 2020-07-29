package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocalLinkInformation {
    @JsonProperty("port_id")
    private  String port_id;
    @JsonProperty("switch_id")
    private String switch_id;
    @JsonProperty("switch_info")
    private String switch_info;

    public String getPort_id() {
        return port_id;
    }

    public void setPort_id(String port_id) {
        this.port_id = port_id;
    }

    public String getSwitch_id() {
        return switch_id;
    }

    public void setSwitch_id(String switch_id) {
        this.switch_id = switch_id;
    }

    public String getSwitch_info() {
        return switch_info;
    }

    public void setSwitch_info(String switch_info) {
        this.switch_info = switch_info;
    }
}
