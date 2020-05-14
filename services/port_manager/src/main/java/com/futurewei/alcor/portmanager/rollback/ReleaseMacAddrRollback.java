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
package com.futurewei.alcor.portmanager.rollback;

import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;

/**
 * When the release of the mac address is successful, but when the release
 * of the mac address needs to be rolled back due to some exception,
 * the doRollback() interface of ReleaseMacAddrRollback is called.
 */
public class ReleaseMacAddrRollback extends AbstractMacAddrRollback {
    public ReleaseMacAddrRollback(MacManagerRestClient macManagerRestClient) {
        super(macManagerRestClient);
    }

    @Override
    public void doRollback() throws Exception {
        for (MacState macState: releasedMacs) {
            macManagerRestClient.allocateMacAddress(
                    macState.getProjectId(),
                    macState.getVpcId(),
                    macState.getPortId(),
                    macState.getMacAddress());
        }
    }
}
