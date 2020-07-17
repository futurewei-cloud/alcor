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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.exception.AllocateMacAddrException;
import com.futurewei.alcor.portmanager.request.*;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.ArrayList;
import java.util.List;

public class MacProcessor extends AbstractProcessor {
    void allocateRandomMacAddressCallback(IRestRequest request) throws AllocateMacAddrException {
        List<MacState> macStates = ((AllocateRandomMacRequest) request).getMacStates();
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
            IRestRequest allocateRandomMacRequest = new AllocateRandomMacRequest(
                    context, randomMacAddresses);
            context.getRequestManager().sendRequestAsync(
                    allocateRandomMacRequest, this::allocateRandomMacAddressCallback);
        }

        if (fixedMacAddresses.size() > 0) {
            IRestRequest allocateFixedMacRequest = new AllocateFixedMacRequest(context, fixedMacAddresses);
            context.getRequestManager().sendRequestAsync(allocateFixedMacRequest, null);
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

            IRestRequest updateMacRequest = new UpdateMacRequest(context, newMacState, oldMacState);
            context.getRequestManager().sendRequestAsync(updateMacRequest, null);

            oldPortEntity.setMacAddress(newMacAddress);
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

        IRestRequest releaseMacRequest = new ReleaseMacRequest(context, macStates);
        context.getRequestManager().sendRequestAsync(releaseMacRequest, null);
    }
}
