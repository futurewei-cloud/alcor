package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.Set;

@Data
public class InternalPortEntityNB extends PortEntity {

    @JsonProperty("neighbor_host_ip")
    private String neighborIp;

    @JsonProperty("binding_host_ip")
    private String bindingHostIP;

    private Set<SubnetEntity> subnetEntities;
    private Set<VpcEntity> vpcEntities;

    public Set<VpcEntity> getVpcEntities() {
        return vpcEntities;
    }

    public void setVpcEntities(Set<VpcEntity> vpcEntities) {
        this.vpcEntities = vpcEntities;
    }

    public Set<SubnetEntity> getSubnetEntities() {
        return subnetEntities;
    }

    public void setSubnetEntities(Set<SubnetEntity> subnetEntities) {
        this.subnetEntities = subnetEntities;
    }

    public String getBindingHostIP() {
        return bindingHostIP;
    }

    public String getNeighborIp() {
        return neighborIp;
    }

    public void setNeighborIp(String neighborIp) {
        this.neighborIp = neighborIp;
    }

    public void setBindingHostIP(String bindingHostIP) {
        this.bindingHostIP = bindingHostIP;
    }

}
