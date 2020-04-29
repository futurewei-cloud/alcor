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

import com.futurewei.alcor.common.entity.MacState;
import com.futurewei.alcor.common.rest.MacAddressRest;

public class AllocateMacAddrRollback extends AbstractMacAddrRollback {

    public AllocateMacAddrRollback(MacAddressRest macAddressRest) {
        super(macAddressRest);
    }

    @Override
    public void doRollback() throws Exception {
        for (MacState macState: allocatedMacs) {
            macAddressRest.releaseMacAddress(macState.getMacAddress());
        }
    }


}
