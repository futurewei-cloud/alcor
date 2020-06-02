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
import com.futurewei.alcor.portmanager.exception.AllocateMacAddrException;
import com.futurewei.alcor.portmanager.rollback.*;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.restclient.MacManagerRestClient;
import java.util.Stack;

public class MacManagerProxy {
    private MacManagerRestClient macManagerRestClient;
    private Stack<Rollback> rollbacks;

    public MacManagerProxy(Stack<Rollback> rollbacks) {
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

    private MacState newMacState(String projectId, String vpcId, String portId, String macAddress) {
        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setVpcId(vpcId);
        macState.setPortId(portId);
        macState.setMacAddress(macAddress);

        return macState;
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
    public MacState allocateRandomMacAddress(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;

        MacStateJson macStateJson = allocateMacAddress(
                portEntity.getProjectId(),
                portEntity.getNetworkId(),
                portEntity.getId(),
                null);

        if (macStateJson == null || macStateJson.getMacState() == null) {
            throw new AllocateMacAddrException();
        }

        portEntity.setMacAddress(macStateJson.getMacState().getMacAddress());

        addMacAddrRollback(new AllocateMacAddrRollback(macManagerRestClient),
                macStateJson.getMacState());

        return macStateJson.getMacState();
    }

    /**
     * Allocate a fixed mac address from mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacState allocateFixedMacAddress(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        String macAddress = portEntity.getMacAddress();

        MacStateJson macStateJson = allocateMacAddress(
                portEntity.getProjectId(),
                portEntity.getNetworkId(),
                portEntity.getId(),
                macAddress);

        if (macStateJson == null || macStateJson.getMacState() == null) {
            throw new AllocateMacAddrException();
        }

        portEntity.setMacAddress(macStateJson.getMacState().getMacAddress());

        addMacAddrRollback(new AllocateMacAddrRollback(macManagerRestClient),
                macStateJson.getMacState());

        return macStateJson.getMacState();
    }

    /**
     * Release a mac address to mac manager service
     * @param args PortState
     * @return MacStateJson
     * @throws Exception Rest request exception
     */
    public MacState releaseMacAddress(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;

        macManagerRestClient.releaseMacAddress(portEntity.getMacAddress());

        MacState macState = new MacState(portEntity.getMacAddress(),
                portEntity.getProjectId(),
                portEntity.getNetworkId(),
                portEntity.getId(),
                null);

        addMacAddrRollback(new ReleaseMacAddrRollback(macManagerRestClient), macState);

        return macState;
    }

    public MacState updateMacAddress(Object arg1, Object arg2) throws Exception {
        PortEntity oldPortEntity = (PortEntity)arg1;
        PortEntity newPortEntity = (PortEntity)arg2;
        String macAddress = newPortEntity.getMacAddress();

        macManagerRestClient.updateMacAddress(newPortEntity.getProjectId(),
                newPortEntity.getNetworkId(),
                newPortEntity.getId(),
                macAddress);

        MacState oldMacState = new MacState(oldPortEntity.getMacAddress(),
                oldPortEntity.getProjectId(),
                oldPortEntity.getNetworkId(),
                oldPortEntity.getId(),
                null);

        addMacAddrRollback(new ReleaseMacAddrRollback(macManagerRestClient), oldMacState);

        return new MacState(newPortEntity.getMacAddress(),
                newPortEntity.getProjectId(),
                newPortEntity.getNetworkId(),
                newPortEntity.getId(),
                null);
    }
}
