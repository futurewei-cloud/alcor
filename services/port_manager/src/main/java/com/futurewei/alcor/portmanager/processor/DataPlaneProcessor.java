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

import com.futurewei.alcor.portmanager.request.CreateNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;
import java.util.stream.Collectors;

public class DataPlaneProcessor extends AbstractProcessor {
    @Override
    void createProcess(List<PortEntity> portEntities) {
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities()
                .stream().map(NetworkConfig.ExtendPortEntity::getInternalPortEntity)
                .collect(Collectors.toList());
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setVpcEntities(networkConfig.getVpcEntities());
        networkConfiguration.setSubnetEntities(networkConfig.getSubnetEntities());
        networkConfiguration.setSecurityGroups(networkConfig.getSecurityGroups());
        networkConfiguration.setPortEntities(internalPortEntities);

        UpstreamRequest createNetworkConfigRequest = new CreateNetworkConfigRequest(networkConfiguration);
        sendRequest(createNetworkConfigRequest, null);
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
