/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
                portEntity.getVpcId(),
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
                portEntity.getVpcId(),
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
                portEntity.getVpcId(),
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
                newPortEntity.getVpcId(),
                newPortEntity.getId(),
                macAddress);

        MacState oldMacState = new MacState(oldPortEntity.getMacAddress(),
                oldPortEntity.getProjectId(),
                oldPortEntity.getVpcId(),
                oldPortEntity.getId(),
                null);

        addMacAddrRollback(new ReleaseMacAddrRollback(macManagerRestClient), oldMacState);

        return new MacState(newPortEntity.getMacAddress(),
                newPortEntity.getProjectId(),
                newPortEntity.getVpcId(),
                newPortEntity.getId(),
                null);
    }
}
