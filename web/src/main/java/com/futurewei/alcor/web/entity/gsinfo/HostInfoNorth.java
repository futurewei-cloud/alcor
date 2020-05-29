package com.futurewei.alcor.web.entity.gsinfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HostInfoNorth {
    @JsonProperty("binding:host_id")
    private String bindingHostId;

    public String getBindingHostId() {
        return bindingHostId;
    }

    public void setBindingHostId(String bindingHostId) {
        this.bindingHostId = bindingHostId;
    }

    public String getBindingHostIp() {
        return bindingHostIp;
    }

    public void setBindingHostIp(String bindingHostIp) {
        this.bindingHostIp = bindingHostIp;
    }

    public String getBindingProfile() {
        return bindingProfile;
    }

    public void setBindingProfile(String bindingProfile) {
        this.bindingProfile = bindingProfile;
    }

    public String getBindingVnicType() {
        return bindingVnicType;
    }

    public void setBindingVnicType(String bindingVnicType) {
        this.bindingVnicType = bindingVnicType;
    }

    public String getNetworkNamespace() {
        return networkNamespace;
    }

    public void setNetworkNamespace(String networkNamespace) {
        this.networkNamespace = networkNamespace;
    }

    @JsonProperty("binding:host_ip")
    private String bindingHostIp;

    @JsonProperty("binding:profile")
    private String bindingProfile;

    @JsonProperty("binding:vnic_type")
    private String bindingVnicType;  //normal, macvtap, direct, baremetal, direct-physical

    @JsonProperty("network_ns")
    private String networkNamespace;

}
