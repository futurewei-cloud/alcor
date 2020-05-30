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

import com.futurewei.alcor.portmanager.util.SpringContextUtil;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortEntity;
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

    private MacStateJson allocateMacAddress(String projectId, String vpcId, String portId, String macAddress) throws Exception {
        return macManagerRestClient.allocateMacAddress(projectId, vpcId, portId, macAddress);
    }

    /**
     * Allocate a random mac address from mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacStateJson allocateRandomMacAddress(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;

        MacStateJson macStateJson = allocateMacAddress(
<<<<<<< HEAD
                portEntity.getProjectId(),
                portEntity.getVpcId(),
                portEntity.getId(),
=======
                portState.getProjectId(),
                portState.getNetworkId(),
                portState.getId(),
>>>>>>> add some missing fileds to PortState
                null);

        portEntity.setMacAddress(macStateJson.getMacState().getMacAddress());

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
        PortEntity portEntity = (PortEntity)args;
        String macAddress = portEntity.getMacAddress();

        MacStateJson macStateJson = allocateMacAddress(
<<<<<<< HEAD
                portEntity.getProjectId(),
                portEntity.getVpcId(),
                portEntity.getId(),
=======
                portState.getProjectId(),
                portState.getNetworkId(),
                portState.getId(),
>>>>>>> add some missing fileds to PortState
                macAddress);

        portEntity.setMacAddress(macStateJson.getMacState().getMacAddress());

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
        PortEntity portEntity = (PortEntity)args;

        macManagerRestClient.releaseMacAddress(portEntity.getMacAddress());

        MacState macState = new MacState();
<<<<<<< HEAD
        macState.setProjectId(portEntity.getProjectId());
        macState.setVpcId(portEntity.getVpcId());
        macState.setPortId(portEntity.getId());
        macState.setMacAddress(portEntity.getMacAddress());
=======
        macState.setProjectId(portState.getProjectId());
        macState.setVpcId(portState.getNetworkId());
        macState.setPortId(portState.getId());
        macState.setMacAddress(portState.getMacAddress());
>>>>>>> add some missing fileds to PortState

        addMacAddrRollback(new ReleaseMacAddrRollback(macManagerRestClient), macState);

        return new MacStateJson(macState);
    }
}
