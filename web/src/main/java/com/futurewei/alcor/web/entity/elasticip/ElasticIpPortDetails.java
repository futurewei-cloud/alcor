package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticIpPortDetails {
    @JsonProperty("name")
    private String name;

    @JsonProperty("network_id")
    private String networkId;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("admin_state_up")
    private Boolean adminStateUp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_owner")
    private String deviceOwner;

    public ElasticIpPortDetails() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Boolean getAdminStateUp() {
        return adminStateUp;
    }

    public void setAdminStateUp(Boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }
}
