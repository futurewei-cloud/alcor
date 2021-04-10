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

package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.exception.AllocateMacAddrException;
import com.futurewei.alcor.portmanager.request.*;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.ArrayList;
import java.util.List;

@AfterProcessor(PortProcessor.class)
public class MacProcessor extends AbstractProcessor {
    void allocateRandomMacAddressCallback(IRestRequest request) throws AllocateMacAddrException {
        List<MacState> macStates = ((AllocateMacAddressRequest) request).getResult();
        List<PortEntity> unassignedMacPorts = request.getContext().getUnassignedMacPorts();
        if (macStates.size() != unassignedMacPorts.size()) {
            throw new AllocateMacAddrException();
        }

        int index = 0;
        for (PortEntity portEntity: unassignedMacPorts) {
            portEntity.setMacAddress(macStates.get(index).getMacAddress());
        }
    }

    @Override
    void createProcess(PortContext context) {
        List<MacState> fixedMacAddresses = new ArrayList<>();
        List<MacState> randomMacAddresses = new ArrayList<>();

        List<PortEntity> unassignedMacPorts = new ArrayList<>();

        for (PortEntity portEntity: context.getPortEntities()){
            String macAddress = portEntity.getMacAddress();
            if (macAddress != null) {
                MacState macState = new MacState(macAddress, context.getProjectId(),
                        portEntity.getVpcId(), portEntity.getId(), null);
                fixedMacAddresses.add(macState);
            } else {
                unassignedMacPorts.add(portEntity);
                MacState macState = new MacState(null, context.getProjectId(),
                        portEntity.getVpcId(), portEntity.getId(), null);
                randomMacAddresses.add(macState);
            }
        }

        context.setUnassignedMacPorts(unassignedMacPorts);

        if (randomMacAddresses.size() > 0) {
            IRestRequest allocateMacAddressRequest = new AllocateMacAddressRequest(
                    context, randomMacAddresses);
            context.getRequestManager().sendRequestAsync(
                    allocateMacAddressRequest, this::allocateRandomMacAddressCallback);
        }

        if (fixedMacAddresses.size() > 0) {
            IRestRequest allocateMacAddressRequest = new AllocateMacAddressRequest(context, fixedMacAddresses);
            context.getRequestManager().sendRequestAsync(allocateMacAddressRequest, null);
        }
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        String newMacAddress = newPortEntity.getMacAddress();
        String oldMacAddress = oldPortEntity.getMacAddress();

        if (newMacAddress != null && !newMacAddress.equals(oldMacAddress)) {
            MacState newMacState = new MacState(newMacAddress,
                    context.getProjectId(),
                    newPortEntity.getVpcId(),
                    newPortEntity.getId(),
                    null);
            MacState oldMacState = new MacState(oldMacAddress,
                    context.getProjectId(),
                    oldPortEntity.getVpcId(),
                    newPortEntity.getId(),
                    null);

            IRestRequest updateMacRequest = new UpdateMacAddressRequest(context, newMacState, oldMacState);
            context.getRequestManager().sendRequestAsync(updateMacRequest, null);
        }
    }

    @Override
    void deleteProcess(PortContext context) {
        List<MacState> macStates = new ArrayList<>();

        for (PortEntity portEntity: context.getPortEntities()) {
            if (portEntity.getMacAddress() == null) {
                continue;
            }

            MacState macState = new MacState(portEntity.getMacAddress(),
                    portEntity.getProjectId(),
                    portEntity.getVpcId(),
                    portEntity.getId(),
                    null);
            macStates.add(macState);
        }

        IRestRequest releaseMacRequest = new ReleaseMacAddressRequest(context, macStates);
        context.getRequestManager().sendRequestAsync(releaseMacRequest, null);
    }
}
