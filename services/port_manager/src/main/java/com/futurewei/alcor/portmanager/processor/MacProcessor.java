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
import com.futurewei.alcor.portmanager.request.AllocateFixedMacRequest;
import com.futurewei.alcor.portmanager.request.AllocateRandomMacRequest;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.ArrayList;
import java.util.List;

public class MacProcessor extends AbstractProcessor {
    private List<PortEntity> targetPorts;

    void allocateRandomMacAddressCallback(UpstreamRequest request) throws AllocateMacAddrException {
        List<MacState> macStates = ((AllocateRandomMacRequest) request).getMacStates();
        if (macStates.size() != targetPorts.size()) {
            throw new AllocateMacAddrException();
        }

        int index = 0;
        for (PortEntity portEntity: targetPorts) {
            portEntity.setMacAddress(macStates.get(index).getMacAddress());
        }
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        List<MacState> fixedMacAddresses = new ArrayList<>();
        List<MacState> randomMacAddresses = new ArrayList<>();

        targetPorts = new ArrayList<>();

        portEntities.stream().forEach((e) -> {
            String macAddress = e.getMacAddress();
            if (macAddress != null) {
                //macAddresses.add(macAddress);
                MacState macState = new MacState(macAddress, e.getProjectId(),
                        e.getVpcId(), e.getId(), null);
                fixedMacAddresses.add(macState);
            } else {
                targetPorts.add(e);
                MacState macState = new MacState(null, e.getProjectId(),
                        e.getVpcId(), e.getId(), null);
                randomMacAddresses.add(macState);
            }
        });

        if (randomMacAddresses.size() > 0) {
            UpstreamRequest allocateRandomMacRequest = new AllocateRandomMacRequest(randomMacAddresses);
            sendRequest(allocateRandomMacRequest, this::allocateRandomMacAddressCallback);
        }

        if (fixedMacAddresses.size() > 0) {
            UpstreamRequest allocateFixedMacRequest = new AllocateFixedMacRequest(fixedMacAddresses);
            sendRequest(allocateFixedMacRequest, null);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
