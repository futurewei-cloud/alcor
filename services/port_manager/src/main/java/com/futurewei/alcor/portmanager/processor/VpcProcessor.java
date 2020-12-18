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
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;
import java.util.stream.Collectors;

@AfterProcessor(PortProcessor.class)
public class VpcProcessor extends AbstractProcessor {
    private void fetchVpcListCallback(IRestRequest request) {
        List<VpcEntity> vpcEntities = ((FetchVpcRequest) request).getVpcEntities();
        request.getContext().getNetworkConfig().setVpcEntities(vpcEntities);
    }

    private void getVpcEntities(PortContext context, List<PortEntity> portEntities) {
        Set<String> vpcIds = portEntities
                .stream()
                .filter(p -> p.getVpcId() != null)
                .map(PortEntity::getVpcId)
                .collect(Collectors.toSet());

        if (vpcIds.size() > 0) {
            IRestRequest fetchVpcRequest = new FetchVpcRequest(context, new ArrayList<>(vpcIds));
            context.getRequestManager().sendRequestAsync(fetchVpcRequest, this::fetchVpcListCallback);
        }
    }

    @Override
    void createProcess(PortContext context) {
        getVpcEntities(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity portEntity = context.getNewPortEntity();
        getVpcEntities(context, Collections.singletonList(portEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        getVpcEntities(context, context.getPortEntities());
    }
}
