package com.futurewei.alcor.subnet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

@Data
public class MacState extends CustomerResource {

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("mac")
    private String mac;

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
