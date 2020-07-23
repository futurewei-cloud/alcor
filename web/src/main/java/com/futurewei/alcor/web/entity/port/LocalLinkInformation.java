package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocalLinkInformation {
    @JsonProperty("port_id")
    private  String port_id="Ethernet3/1";
    @JsonProperty("switch_id")
    private String switch_id="0a:1b:2c:3d:4e:5f";
    @JsonProperty("switch_info")
    private String switch_info="switch1";
}
