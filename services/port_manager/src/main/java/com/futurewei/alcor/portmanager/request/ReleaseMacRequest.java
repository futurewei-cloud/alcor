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

import java.util.List;

public class ReleaseMacRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(ReleaseMacRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private List<MacState> macStates;

    public ReleaseMacRequest(PortContext context, List<MacState> macStates) {
        super(context);
        this.macStates = macStates;
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        for (MacState macState: macStates) {
            macManagerRestClient.releaseMacAddress(macState.getMacAddress());
        }
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("ReleaseMacRequest rollback, macStates: {}", macStates);
        for (MacState macState: macStates) {
            macManagerRestClient.allocateMacAddress(macState.getProjectId(),
                    macState.getVpcId(),
                    macState.getPortId(),
                    macState.getMacAddress());
        }
    }
}
