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

import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;
import java.util.Map;

public class PortConfigCache {
    private Map<String, String> portVpcIdMap;
    private Map<String, List<PortEntity.FixedIp>> portFixedIpsMap;
    private Map<String, List<String>> portSecurityGroupIdsMap;
    private Map<String, String> portBindingHostIdMap;

    public Map<String, String> getPortVpcIdMap() {
        return portVpcIdMap;
    }

    public void setPortVpcIdMap(Map<String, String> portVpcIdMap) {
        this.portVpcIdMap = portVpcIdMap;
    }

    public Map<String, List<PortEntity.FixedIp>> getPortFixedIpsMap() {
        return portFixedIpsMap;
    }

    public void setPortFixedIpsMap(Map<String, List<PortEntity.FixedIp>> portFixedIpsMap) {
        this.portFixedIpsMap = portFixedIpsMap;
    }

    public Map<String, List<String>> getPortSecurityGroupIdsMap() {
        return portSecurityGroupIdsMap;
    }

    public void setPortSecurityGroupIdsMap(Map<String, List<String>> portSecurityGroupIdsMap) {
        this.portSecurityGroupIdsMap = portSecurityGroupIdsMap;
    }

    public Map<String, String> getPortBindingHostIdMap() {
        return portBindingHostIdMap;
    }

    public void setPortBindingHostIdMap(Map<String, String> portBindingHostIdMap) {
        this.portBindingHostIdMap = portBindingHostIdMap;
    }
}
