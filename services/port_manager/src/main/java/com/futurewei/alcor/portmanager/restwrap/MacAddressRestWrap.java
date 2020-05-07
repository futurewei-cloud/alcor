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
package com.futurewei.alcor.portmanager.restwrap;

import com.futurewei.alcor.web.entity.MacState;
import com.futurewei.alcor.web.entity.MacStateJson;
import com.futurewei.alcor.web.entity.PortState;
import com.futurewei.alcor.web.rest.MacAddressRest;
import com.futurewei.alcor.portmanager.rollback.AllocateMacAddrRollback;
import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import com.futurewei.alcor.portmanager.rollback.ReleaseMacAddrRollback;

import java.util.Stack;


public class MacAddressRestWrap {
    private MacAddressRest macAddressRest;
    private Stack<PortStateRollback> rollbacks;

    public MacAddressRestWrap(MacAddressRest macAddressRest, Stack<PortStateRollback> rollbacks) {
        this.macAddressRest = macAddressRest;
        this.rollbacks = rollbacks;
    }

    public MacStateJson allocateMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;

        MacStateJson result = macAddressRest.allocateMacAddress(portState.getProjectId(), portState.getVpcId(), portState.getId());
        portState.setMacAddress(result.getMacState().getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(portState.getProjectId());
        macState.setVpcId(portState.getVpcId());
        macState.setMacAddress(portState.getMacAddress());

        AllocateMacAddrRollback rollback = new AllocateMacAddrRollback(macAddressRest);
        rollback.putAllocatedMacAddress(macState);
        rollbacks.push(rollback);

        return result;
    }

    public MacStateJson verifyMacAddress(Object args) {
        //FIXME: Not support yet
        return null;
    }

    public MacStateJson releaseMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;

        macAddressRest.releaseMacAddress(portState.getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(portState.getProjectId());
        macState.setVpcId(portState.getVpcId());
        macState.setMacAddress(portState.getMacAddress());

        ReleaseMacAddrRollback rollback = new ReleaseMacAddrRollback(macAddressRest);
        rollback.putReleasedMacAddress(macState);

        rollbacks.push(rollback);

        return new MacStateJson(macState);
    }
}
