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
import com.futurewei.alcor.portmanager.exception.AllocateMacAddrException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AllocateRandomMacRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(AllocateRandomMacRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private List<MacState> randomMacAddresses;
    private List<MacState> macStates;

    public AllocateRandomMacRequest(PortContext context, List<MacState> randomMacAddresses) {
        super(context);
        this.randomMacAddresses = randomMacAddresses;
        this.macStates = new ArrayList<>();
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    public List<MacState> getMacStates() {
        return macStates;
    }

    @Override
    public void send() throws Exception {
        //TODO: Instead by allocateMacAddresses interface
        for (MacState macState: randomMacAddresses) {
            MacStateJson macStateJson = macManagerRestClient.allocateMacAddress(
                    macState.getProjectId(),
                    macState.getVpcId(),
                    macState.getPortId(),
                    macState.getMacAddress());
            if (macStateJson == null || macStateJson.getMacState() == null) {
                throw new AllocateMacAddrException();
            }

            macStates.add(macStateJson.getMacState());
        }
    }



    @Override
    public void rollback() throws Exception {
        LOG.info("AllocateRandomMacRequest rollback, macStates: {}", macStates);
        //TODO: Instead by releaseMacAddresses interface
        for (MacState macState: macStates) {
            macManagerRestClient.releaseMacAddress(macState.getMacAddress());
        }
    }
}
