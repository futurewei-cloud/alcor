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

import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.portmanager.utils.BeanUtil;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortState;
import com.futurewei.alcor.web.rest.MacAddressRest;
import java.util.Stack;

public class MacAddressRestWrap {
    private MacAddressRest macAddressRest;
    private Stack<PortStateRollback> rollbacks;

    public MacAddressRestWrap(Stack<PortStateRollback> rollbacks) {
        macAddressRest = BeanUtil.getBean(MacAddressRest.class);
        this.rollbacks = rollbacks;
    }

    private void addMacAddrRollback(AbstractMacAddrRollback rollback, MacState macState) {
        if (rollback instanceof AllocateMacAddrRollback) {
            rollback.putAllocatedMacAddress(macState);
        } else {
            rollback.putReleasedMacAddress(macState);
        }

        rollbacks.push(rollback);
    }

    public MacStateJson allocateMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;

        MacStateJson result = macAddressRest.allocateMacAddress(portState.getProjectId(), portState.getVpcId(), portState.getId());
        portState.setMacAddress(result.getMacState().getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(portState.getProjectId());
        macState.setVpcId(portState.getVpcId());
        macState.setMacAddress(portState.getMacAddress());

        addMacAddrRollback(new AllocateMacAddrRollback(macAddressRest), macState);

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

        addMacAddrRollback(new ReleaseMacAddrRollback(macAddressRest), macState);

        return new MacStateJson(macState);
    }
}
