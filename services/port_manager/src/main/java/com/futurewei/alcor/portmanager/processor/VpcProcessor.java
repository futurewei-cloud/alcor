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

import com.futurewei.alcor.portmanager.request.FetchVpcRequest;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;

public class VpcProcessor extends AbstractProcessor {
    private void fetchVpcListCallback(UpstreamRequest request) {
        List<VpcEntity> vpcEntities = ((FetchVpcRequest) request).getVpcEntities();
        networkConfig.setVpcEntities(vpcEntities);
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        Set<String> vpcIds = new HashSet<>();

        portEntities.stream().forEach((p) -> {
            String vpcId = p.getVpcId();
            vpcIds.add(vpcId);
        });

        if (vpcIds.size() > 0) {
            UpstreamRequest fetchVpcRequest = new FetchVpcRequest(projectId, new ArrayList<>(vpcIds));
            sendRequest(fetchVpcRequest, this::fetchVpcListCallback);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
