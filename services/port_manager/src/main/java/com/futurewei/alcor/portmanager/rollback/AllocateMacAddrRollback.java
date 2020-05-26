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
 * When the mac address allocation is successful, but when the mac address
 * allocation needs to be rolled back due to some exceptions, the doRollback()
 * interface of AllocateMacAddrRollback is called.
 */
public class AllocateMacAddrRollback extends AbstractMacAddrRollback {

    public AllocateMacAddrRollback(MacManagerRestClient macManagerRestClient) {
        super(macManagerRestClient);
    }

    @Override
    public void doRollback() throws Exception {
        for (MacState macState: allocatedMacs) {
            macManagerRestClient.releaseMacAddress(macState.getMacAddress());
        }
    }


}
