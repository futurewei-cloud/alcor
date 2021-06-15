/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.netwconfigmanager.entity;

import com.futurewei.alcor.schema.Goalstate;

import java.util.*;

import static com.futurewei.alcor.netwconfigmanager.constant.Constants.UNEXPECTED_RESOURCE_ID;

public class ResourceMeta {

    private String ownerId;
    private Set<String> vpcIds;
    private Set<String> subnetIds;
    private Set<String> portIds;
    private Map<String, String> neighborMap;
    private Set<String> securityGroupIds;
    private Set<String> dhcpIds;
    private Set<String> routerIds;
    private Set<String> gatewayIds;

    public ResourceMeta(String ownerId) {
        this.ownerId = ownerId;
        this.vpcIds = new HashSet<>();
        this.subnetIds = new HashSet<>();
        this.portIds = new HashSet<>();
        this.neighborMap = new HashMap<>();
        this.securityGroupIds = new HashSet<>();
        this.dhcpIds = new HashSet<>();
        this.routerIds = new HashSet<>();
        this.gatewayIds = new HashSet<>();
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    ////////////////////////
    // Vpc related methods
    ////////////////////////
    public Set<String> getVpcIds() {
        return this.vpcIds;
    }

    public String getDefaultVpcId() {
        return this.vpcIds != null && this.vpcIds.size() > 0 ? this.vpcIds.iterator().next() : UNEXPECTED_RESOURCE_ID;
    }

    public ResourceMeta addVpcId(String newId) {
        this.vpcIds.add(newId);
        return this;
    }

    public void deleteVpcId(String delId) {
        this.vpcIds.remove(delId);
    }

    ////////////////////////
    // Subnet related methods
    ////////////////////////
    public Set<String> getSubnetIds() {
        return this.subnetIds;
    }

    public String getDefaultSubnetId() {
        return this.subnetIds != null && this.subnetIds.size() > 0 ? this.subnetIds.iterator().next() : UNEXPECTED_RESOURCE_ID;
    }

    public ResourceMeta addSubnetId(String newId) {
        this.subnetIds.add(newId);
        return this;
    }

    public void deleteSubnetId(String delId) {
        this.subnetIds.remove(delId);
    }

    ////////////////////////
    // Port related methods
    ////////////////////////
    public Set<String> getPortIds() {
        return this.portIds;
    }

    public String getDefaultPortId() {
        return this.portIds != null && this.portIds.size() > 0 ? this.portIds.iterator().next() : UNEXPECTED_RESOURCE_ID;
    }

    public ResourceMeta addPortId(String newId) {
        this.portIds.add(newId);
        return this;
    }

    public void deletePortId(String delId) {
        this.portIds.remove(delId);
    }

    ////////////////////////
    // Neighbor related methods
    ////////////////////////
    public Map<String, String> getNeighborIdMap() {
        return this.neighborMap;
    }

    public ResourceMeta addNeighborEntry(String neighborIp, String neighborId) {
        this.neighborMap.put(neighborIp, neighborId);
        return this;
    }

    public void deleteNeighborEntry(String neighborIp) {
        this.neighborMap.remove(neighborIp);
    }

    ////////////////////////
    // Dhcp related methods
    ////////////////////////
    public Set<String> getDhcpIds() {
        return this.dhcpIds;
    }

    public ResourceMeta addDhcpId(String newId) {
        this.dhcpIds.add(newId);
        return this;
    }

    public void deleteDhcpId(String delId) {
        this.dhcpIds.remove(delId);
    }

    ////////////////////////
    // Router related methods
    ////////////////////////
    public Set<String> getRouterIds() {
        return this.routerIds;
    }

    public ResourceMeta addRouterId(String newId) {
        this.routerIds.add(newId);
        return this;
    }

    public void deleteRouterId(String delId) {
        this.routerIds.remove(delId);
    }

    ////////////////////////
    // SG related methods
    ////////////////////////
    public ResourceMeta addSecurityGroupId(String newId) {
        this.securityGroupIds.add(newId);
        return this;
    }

    ////////////////////////
    // Gateway related methods
    ////////////////////////
    public ResourceMeta addGatewayId(String newId) {
        this.gatewayIds.add(newId);
        return this;
    }
}
