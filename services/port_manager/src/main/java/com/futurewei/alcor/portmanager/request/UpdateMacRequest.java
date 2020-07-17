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
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateMacRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMacRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private MacState newMacState;
    private MacState oldMacState;

    public UpdateMacRequest(PortContext context, MacState newMacState, MacState oldMacState) {
        super(context);
        this.newMacState = newMacState;
        this.oldMacState = oldMacState;
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        macManagerRestClient.updateMacAddress(context.getProjectId(),
                newMacState.getVpcId(),
                newMacState.getPortId(),
                newMacState.getMacAddress());
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("UpdateMacRequest rollback, oldMacState: {}", oldMacState);
        macManagerRestClient.updateMacAddress(context.getProjectId(),
                oldMacState.getVpcId(),
                oldMacState.getPortId(),
                oldMacState.getMacAddress());
    }
}
