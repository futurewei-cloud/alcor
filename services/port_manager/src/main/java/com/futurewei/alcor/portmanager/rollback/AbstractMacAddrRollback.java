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
import com.futurewei.alcor.web.rest.MacAddressRest;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractMacAddrRollback implements PortStateRollback {
    protected MacAddressRest macAddressRest;

    protected List<MacState> allocatedMacs = new ArrayList<>();
    protected List<MacState> releasedMacs = new ArrayList<>();

    public AbstractMacAddrRollback(MacAddressRest macAddressRest) {
        this.macAddressRest = macAddressRest;
    }

    public abstract void doRollback() throws Exception;

    public void putAllocatedMacAddress(MacState macState) {
        allocatedMacs.add(macState);
    }

    public void putReleasedMacAddress(MacState macState) {
        releasedMacs.add(macState);
    }

    public void putAllocatedMacAddresses(List<MacState> macStates) {
        allocatedMacs.addAll(macStates);
    }

    public void putReleasedMacAddresses(List<MacState> macStates) {
        releasedMacs.addAll(macStates);
    }
}
