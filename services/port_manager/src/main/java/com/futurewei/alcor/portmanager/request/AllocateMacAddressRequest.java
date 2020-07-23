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
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AllocateMacAddressRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(AllocateMacAddressRequest.class);

    private MacManagerRestClient macManagerRestClient;
    private List<MacState> macStates;
    private List<MacState> result;

    public AllocateMacAddressRequest(PortContext context, List<MacState> macStates) {
        super(context);
        this.macStates = macStates;
        this.result = new ArrayList<>();
        this.macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
    }

    public List<MacState> getResult() {
        return result;
    }

    @Override
    public void send() throws Exception {
        if (macStates.size() == 1) {
            MacStateJson macStateJson = macManagerRestClient.allocateMacAddress(macStates.get(0));
            if (macStateJson == null || macStateJson.getMacState() == null) {
                throw new AllocateMacAddrException();
            }

            result.add(macStateJson.getMacState());
        } else {
            MacStateBulkJson macStateBulkJson = macManagerRestClient.allocateMacAddressBulk(macStates);
            if (macStateBulkJson == null || macStateBulkJson.getMacStates() == null) {
                throw new AllocateMacAddrException();
            }

            result.addAll(macStateBulkJson.getMacStates());
        }
    }



    @Override
    public void rollback() throws Exception {
        LOG.info("AllocateRandomMacRequest rollback, macStates: {}", result);
        //TODO: Instead by releaseMacAddresses interface
        for (MacState macState: result) {
            macManagerRestClient.releaseMacAddress(macState.getMacAddress());
        }
    }
}
