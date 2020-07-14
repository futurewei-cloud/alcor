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

import com.futurewei.alcor.portmanager.request.FetchSubnetRequest;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;

import java.util.ArrayList;
import java.util.List;

public class SubnetProcessor extends AbstractProcessor {
    private void fetchSubnetCallBack(UpstreamRequest request) {
        List<SubnetEntity> subnetEntities = ((FetchSubnetRequest) (request)).getSubnetEntities();

        List<InternalSubnetEntity> internalSubnetEntities = new ArrayList<>();
        subnetEntities.stream().forEach((s) -> {
            InternalSubnetEntity internalSubnetEntity = new InternalSubnetEntity(s, null);
            internalSubnetEntities.add(internalSubnetEntity);
        });

        networkConfig.setSubnetEntities(internalSubnetEntities);
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        List<String> subnetIds = new ArrayList<>();

        portEntities.stream().forEach((p) -> {
            List<PortEntity.FixedIp> fixedIps = p.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp: fixedIps) {
                    subnetIds.add(fixedIp.getSubnetId());
                }
            }
        });

        if (subnetIds.size() > 0) {
            UpstreamRequest fetchSubnetRequest = new FetchSubnetRequest(projectId, subnetIds);
            sendRequest(fetchSubnetRequest, this::fetchSubnetCallBack);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
