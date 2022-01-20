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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.repo.IPortRepository;
import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.request.RequestManager;
import com.futurewei.alcor.web.entity.ip.IpAddrUpdateRequest;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortContext {
    private PortConfigCache portConfigCache;
    private NetworkConfig networkConfig;
    private String projectId;
    private IPortRepository portRepository;
    private RequestManager requestManager;
    private List<PortEntity> unassignedIpPorts;
    private Map<String, PortEntity.FixedIp> hasIpFixedIps;
    private List<PortEntity.FixedIp> hasSubnetFixedIps;
    private List<PortEntity> unassignedMacPorts;
    private List<PortEntity> portEntities; //For add and delete
    private PortEntity oldPortEntity; //For update
    private PortEntity newPortEntity; //For update
    private Map<String, List<SubnetEntity>> routerSubnetEntities;
    private Map<String, InternalRouterInfo> routers;
    private List<NodeInfo> nodeInfos;
    private List<PortEntity.FixedIp> newFixedIps;
    private List<PortEntity.FixedIp> oldFixedIps;
    private List<IpAddrUpdateRequest> fixedIpsresult;
    private String defaultSgId;

    public PortContext() {

    }

    public PortContext(PortConfigCache portConfigCache, String projectId, IPortRepository portRepository) {
        this.portConfigCache = portConfigCache;
        this.projectId = projectId;
        this.portRepository = portRepository;
        this.networkConfig = new NetworkConfig();
        this.requestManager = new RequestManager();
    }

    public PortConfigCache getPortConfigCache() {
        return portConfigCache;
    }

    public void setPortConfigCache(PortConfigCache portConfigCache) {
        this.portConfigCache = portConfigCache;
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    public void setNetworkConfig(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public IPortRepository getPortRepository() {
        return portRepository;
    }

    public void setPortRepository(PortRepository portRepository) {
        this.portRepository = portRepository;
    }

    public List<PortEntity> getPortEntities() {
        return portEntities;
    }

    public void setPortEntities(List<PortEntity> portEntities) {
        this.portEntities = portEntities;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public List<PortEntity> getUnassignedIpPorts() {
        return unassignedIpPorts;
    }

    public void setUnassignedIpPorts(List<PortEntity> unassignedIpPorts) {
        this.unassignedIpPorts = unassignedIpPorts;
    }

    public Map<String, PortEntity.FixedIp> getHasIpFixedIps() {
        return hasIpFixedIps;
    }

    public void setHasIpFixedIps(Map<String, PortEntity.FixedIp> hasIpFixedIps) {
        this.hasIpFixedIps = hasIpFixedIps;
    }

    public List<PortEntity.FixedIp> getHasSubnetFixedIps() {
        return hasSubnetFixedIps;
    }

    public void setHasSubnetFixedIps(List<PortEntity.FixedIp> hasSubnetFixedIps) {
        this.hasSubnetFixedIps = hasSubnetFixedIps;
    }

    public List<PortEntity> getUnassignedMacPorts() {
        return unassignedMacPorts;
    }

    public void setUnassignedMacPorts(List<PortEntity> unassignedMacPorts) {
        this.unassignedMacPorts = unassignedMacPorts;
    }

    public PortEntity getOldPortEntity() {
        return oldPortEntity;
    }

    public void setOldPortEntity(PortEntity oldPortEntity) {
        this.oldPortEntity = oldPortEntity;
    }

    public PortEntity getNewPortEntity() {
        return newPortEntity;
    }

    public void setNewPortEntity(PortEntity newPortEntity) {
        this.newPortEntity = newPortEntity;
    }

    public List<SubnetEntity> getRouterSubnetEntities(String vpcId) {
        return (this.routerSubnetEntities != null) ? this.routerSubnetEntities.get(vpcId) : null;
    }

    public void setRouterSubnetEntities(Map<String, List<SubnetEntity>> routerSubnetEntities) {
        this.routerSubnetEntities = routerSubnetEntities;
    }

    public void addRouterSubnetEntities(String vpcId, List<SubnetEntity> subnetEntities) {
        if (this.routerSubnetEntities == null) {
            this.routerSubnetEntities = new HashMap<>();
        }
        this.routerSubnetEntities.put(vpcId, subnetEntities);
    }

    public InternalRouterInfo getRouterByVpcOrSubnetId(String vpcOrSubnetId) {
        return (this.routers != null) ? this.routers.get(vpcOrSubnetId) : null;
    }

    public boolean containRouters() {
        if (this.routers == null || this.routers.isEmpty()) {
            return false;
        }

        return true;
    }

    public void setRouters(Map<String, InternalRouterInfo> routers) {
        this.routers = routers;
    }

    public void addRouter(String routerIdx, InternalRouterInfo router) {
        if (this.routers == null) {
            this.routers = new HashMap<>();
        }
        this.routers.put(routerIdx, router);
    }

    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public void setNodeInfos(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    public List<PortEntity.FixedIp> getNewFixedIps() {
        return newFixedIps;
    }

    public void setNewFixedIps(List<PortEntity.FixedIp> newFixedIps) {
        this.newFixedIps = newFixedIps;
    }

    public List<PortEntity.FixedIp> getOldFixedIps() {
        return oldFixedIps;
    }

    public void setOldFixedIps(List<PortEntity.FixedIp> oldFixedIps) {
        this.oldFixedIps = oldFixedIps;
    }

    public List<IpAddrUpdateRequest> getFixedIpsresult() {
        return fixedIpsresult;
    }

    public void setFixedIpsresult(List<IpAddrUpdateRequest> fixedIpsresult) {
        this.fixedIpsresult = fixedIpsresult;
    }

    public String getDefaultSgId() {
        return defaultSgId;
    }

    public void setDefaultSgId(String defaultSgId) {
        this.defaultSgId = defaultSgId;
    }
}
