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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortState;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import java.util.Stack;

public class MacManagerProxy {
    private MacManagerRestClient macManagerRestClient;
    private Stack<PortStateRollback> rollbacks;

    public MacManagerProxy(Stack<PortStateRollback> rollbacks) {
        macManagerRestClient = SpringContextUtil.getBean(MacManagerRestClient.class);
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

    private MacStateJson allocateMacAddress(String projectId, String vpcId, String portId, String mac) throws Exception {
        return macManagerRestClient.allocateMacAddress(projectId, vpcId, portId, mac);
    }

    /**
     * Allocate a random mac address from mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacStateJson allocateRandomMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;

        MacStateJson macStateJson = allocateMacAddress(
                portState.getProjectId(),
                portState.getVpcId(),
                portState.getId(),
                null);

        portState.setMacAddress(macStateJson.getMacState().getMacAddress());

        addMacAddrRollback(new AllocateMacAddrRollback(macManagerRestClient),
                macStateJson.getMacState());

        return macStateJson;
    }

    /**
     * Allocate a fixed mac address from mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacStateJson allocateFixedMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;
        String macAddress = portState.getMacAddress();

        MacStateJson macStateJson = allocateMacAddress(
                portState.getProjectId(),
                portState.getVpcId(),
                portState.getId(),
                macAddress);

        portState.setMacAddress(macStateJson.getMacState().getMacAddress());

        addMacAddrRollback(new AllocateMacAddrRollback(macManagerRestClient),
                macStateJson.getMacState());

        return macStateJson;
    }

    /**
     * Release a mac address to mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacStateJson releaseMacAddress(Object args) throws Exception {
        PortState portState = (PortState)args;

        macManagerRestClient.releaseMacAddress(portState.getMacAddress());

        MacState macState = new MacState();
        macState.setProjectId(portState.getProjectId());
        macState.setVpcId(portState.getVpcId());
        macState.setPortId(portState.getId());
        macState.setMacAddress(portState.getMacAddress());

        addMacAddrRollback(new ReleaseMacAddrRollback(macManagerRestClient), macState);

        return new MacStateJson(macState);
    }
}
