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
import com.futurewei.alcor.portmanager.request.DeleteNetworkConfigRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UpdateNetworkConfigRequest;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPlaneProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneProcessor.class);

    private NetworkConfiguration buildNetworkConfig(PortContext context) {
        /**
         DataPlaneProcessor needs to wait for all previous Processor runs to
         finish before continuing. Since DataPlaneProcessor is the last Processor
         in the process chain, so it can call waitAllRequestsFinish,when calling
         waitAllRequestsFinish we must make sure that all asynchronous methods have been called
         */
        context.getRequestManager().waitAllRequestsFinish();

        NetworkConfig networkConfig = context.getNetworkConfig();
        if (networkConfig.getPortEntities().size() == 0) {
            return null;
        }

        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        networkConfiguration.setVpcs(networkConfig.getVpcEntities());
        networkConfiguration.setSubnets(networkConfig.getSubnetEntities());
        networkConfiguration.setSecurityGroups(networkConfig.getSecurityGroups());
        networkConfiguration.setPortEntities(networkConfig.getPortEntities());

        LOG.info("Network configuration: {}", networkConfiguration);

        return networkConfiguration;
    }

    private void createNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest createNetworkConfigRequest =
                    new CreateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(createNetworkConfigRequest, null);
        }
    }

    private void updateNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest updateNetworkConfigRequest =
                    new UpdateNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(updateNetworkConfigRequest, null);
        }
    }

    private void deleteNetworkConfig(PortContext context, NetworkConfiguration networkConfig) {
        if (networkConfig != null) {
            IRestRequest deleteNetworkConfigRequest =
                    new DeleteNetworkConfigRequest(context, networkConfig);
            context.getRequestManager().sendRequestAsync(deleteNetworkConfigRequest, null);
        }
    }

    @Override
    void createProcess(PortContext context) {
        createNetworkConfig(context, buildNetworkConfig(context));
    }

    @Override
    void updateProcess(PortContext context) {
        updateNetworkConfig(context, buildNetworkConfig(context));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        deleteNetworkConfig(context, buildNetworkConfig(context));
    }
}
