/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkConfig {
    private List<InternalPortEntity> portEntities;

    private List<VpcEntity> vpcEntities;

    private List<InternalSubnetEntity> subnetEntities;

    private List<SecurityGroup> securityGroups;

    //Key: portIp, value: Neighbor information for port
    private Map<String, NeighborInfo> neighborInfos;

    //Key: portIp, value: All neighbor information for port
    private Map<String, List<NeighborEntry>> neighborTable;

    private List<InternalRouterInfo> routerInfos;

    public static class ExtendPortEntity extends InternalPortEntity {
        private String bindingHostId;

        public ExtendPortEntity(InternalPortEntity internalPortEntity, String bindingHostId) {
            super(internalPortEntity, internalPortEntity.getRoutes(), internalPortEntity.getBindingHostIP());
            this.bindingHostId = bindingHostId;
        }

        public String getBindingHostId() {
            return bindingHostId;
        }

        public void setBindingHostId(String bindingHostId) {
            this.bindingHostId = bindingHostId;
        }
    }

    public NetworkConfig() {
    }

    public List<InternalPortEntity> getPortEntities() {
        return this.portEntities;
    }

    public void setPortEntities(List<InternalPortEntity> portEntities) {
        this.portEntities = portEntities;
    }

    public List<VpcEntity> getVpcEntities() {
        return this.vpcEntities;
    }

    public void setVpcEntities(List<VpcEntity> vpcEntities) {
        this.vpcEntities = vpcEntities;
    }

    public List<InternalSubnetEntity> getSubnetEntities() {
        return this.subnetEntities;
    }

    public void setSubnetEntities(List<InternalSubnetEntity> subnetEntities) {
        this.subnetEntities = subnetEntities;
    }

    public boolean addSubnetEntity(InternalSubnetEntity subnetEntity){
        if(this.subnetEntities == null){
            this.subnetEntities = new ArrayList<>();
        }

        for(InternalSubnetEntity entity: this.subnetEntities){
            if(entity.getId().equals(subnetEntity.getId())){
                return false;
            }
        }

        this.subnetEntities.add(subnetEntity);
        return true;
    }

    public List<SecurityGroup> getSecurityGroups() {
        return this.securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public Map<String, NeighborInfo> getNeighborInfos() {
        return this.neighborInfos;
    }

    public void setNeighborInfos(Map<String, NeighborInfo> neighborInfos) {
        this.neighborInfos = neighborInfos;
    }

    public Map<String, List<NeighborEntry>> getNeighborTable() {
        return this.neighborTable;
    }

    public void addNeighborEntries(String portIp, List<NeighborEntry> neighborEntries) {
        if (this.neighborTable == null) {
            this.neighborTable = new HashMap<>();
        }

        if (!this.neighborTable.containsKey(portIp)) {
            this.neighborTable.put(portIp, new ArrayList<>());
        }

        this.neighborTable.get(portIp).addAll(neighborEntries);
    }

    public List<InternalRouterInfo> getRouterInfos() {
        return this.routerInfos;
    }

    public void setRouterInfos(List<InternalRouterInfo> routerInfos) {
        this.routerInfos = routerInfos;
    }

    public void addRouterEntry(InternalRouterInfo routerInfo) {
        if (this.routerInfos == null) {
            this.routerInfos = new ArrayList<>();
        }
        this.routerInfos.add(routerInfo);
    }
}
