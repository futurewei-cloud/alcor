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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortEntityParser {
    public static void parse(List<PortEntity> portEntities, PortConfigCache portConfigCache) {
        Map<String, String> portVpcIdMap = new HashMap<>();
        Map<String, List<PortEntity.FixedIp>> portFixedIpsMap = new HashMap<>();
        Map<String, List<String>> portSecurityGroupIdsMap = new HashMap<>();
        Map<String, String> portBindingHostIdMap = new HashMap<>();


        for (PortEntity portEntity: portEntities) {
            //Parse vpc id
            portVpcIdMap.put(portEntity.getId(), portEntity.getVpcId());

            //Parse fixedIps
            portFixedIpsMap.put(portEntity.getId(), portEntity.getFixedIps());

            //Parse security group id
            portSecurityGroupIdsMap.put(portEntity.getId(), portEntity.getSecurityGroups());

            //Parse binding host id
            portBindingHostIdMap.put(portEntity.getId(), portEntity.getBindingHostId());
        }

        portConfigCache.setPortVpcIdMap(portVpcIdMap);
        portConfigCache.setPortFixedIpsMap(portFixedIpsMap);
        portConfigCache.setPortSecurityGroupIdsMap(portSecurityGroupIdsMap);
        portConfigCache.setPortBindingHostIdMap(portBindingHostIdMap);
    }
}
