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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateNetworkConfigRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateNetworkConfigRequest.class);

    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfiguration networkConfig;

    public UpdateNetworkConfigRequest(PortContext context, NetworkConfiguration networkConfig) {
        super(context);
        this.networkConfig = networkConfig;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        dataPlaneManagerRestClient.updateNetworkConfig(networkConfig);
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("UpdateNetworkConfigRequest rollback, networkConfig: {}", networkConfig);
        //TODO: how to deal with this rollback ?
    }
}
