package com.futurewei.alcor.subnet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class IPState  extends CustomerResource {

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("subnet_cidr")
    private String subnetCidr;

    @JsonProperty("ip")
    private String ip;

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(String subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
