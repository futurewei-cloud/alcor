package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class InternalPortEntityNB extends PortEntity {

    @JsonProperty("neighbor_info")
    private List<NeighborInfo> neighborIps;

    public List<NeighborInfo> getNeighborIps() {
        return neighborIps;
    }

    public void setNeighborIps(List<NeighborInfo> neighborIps) {
        this.neighborIps = neighborIps;
    }

    @JsonProperty("binding_host_ip")
    private String bindingHostIP;

    private Set<SubnetEntityNB> subnetEntities;
    private Set<VpcEntity> vpcEntities;

    public Set<VpcEntity> getVpcEntities() {
        return vpcEntities;
    }

    public void setVpcEntities(Set<VpcEntity> vpcEntities) {
        this.vpcEntities = vpcEntities;
    }

    public Set<SubnetEntityNB> getSubnetEntities() {
        return subnetEntities;
    }

    public void setSubnetEntities(Set<SubnetEntityNB> subnetEntities) {
        this.subnetEntities = subnetEntities;
    }

    public String getBindingHostIP() {
        return bindingHostIP;
    }



    public void setBindingHostIP(String bindingHostIP) {
        this.bindingHostIP = bindingHostIP;
    }

}
