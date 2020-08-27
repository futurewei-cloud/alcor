/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.repo.PortRepository;
import com.futurewei.alcor.portmanager.request.RequestManager;
import com.futurewei.alcor.web.entity.port.PortEntity;
import java.util.List;
import java.util.Map;

public class PortContext {
    private PortConfigCache portConfigCache;
    private NetworkConfig networkConfig;
    private String projectId;
    private PortRepository portRepository;
    private RequestManager requestManager;
    private List<PortEntity> unassignedIpPorts;
    private Map<String, PortEntity.FixedIp> hasIpFixedIps;
    private List<PortEntity.FixedIp> hasSubnetFixedIps;
    private List<PortEntity> unassignedMacPorts;
    private List<PortEntity> portEntities; //For add and delete
    private PortEntity oldPortEntity; //For update
    private PortEntity newPortEntity; //For update
    private List<String> routerSubnetIds;

    public PortContext() {

    }

    public PortContext(PortConfigCache portConfigCache, String projectId, PortRepository portRepository) {
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

    public PortRepository getPortRepository() {
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

    public List<String> getRouterSubnetIds() {
        return routerSubnetIds;
    }

    public void setRouterSubnetIds(List<String> routerSubnetIds) {
        this.routerSubnetIds = routerSubnetIds;
    }
}
