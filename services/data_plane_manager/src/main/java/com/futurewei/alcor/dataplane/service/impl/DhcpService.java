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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.schema.DHCP;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DhcpService extends ResourceService {
    public void buildDhcpStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (Port.PortState portState: portStates) {
            String macAddress = portState.getConfiguration().getMacAddress();
            List<Port.PortConfiguration.FixedIp> fixedIps = portState.getConfiguration().getFixedIpsList();
            for (Port.PortConfiguration.FixedIp fixedIp: fixedIps) {
                DHCP.DHCPConfiguration.Builder dhcpConfigBuilder = DHCP.DHCPConfiguration.newBuilder();
                dhcpConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
                dhcpConfigBuilder.setMacAddress(macAddress);
                dhcpConfigBuilder.setIpv4Address(fixedIp.getIpAddress());
                //TODO: support ipv6
                //dhcpConfigBuilder.setIpv6Address();
                //dhcpConfigBuilder.setPortHostName();
                //dhcpConfigBuilder.setExtraDhcpOptions();
                //dhcpConfigBuilder.setDnsEntryList();

                DHCP.DHCPState.Builder dhcpStateBuilder = DHCP.DHCPState.newBuilder();
                dhcpStateBuilder.setOperationType(networkConfig.getOpType());
                dhcpStateBuilder.setConfiguration(dhcpConfigBuilder.build());
                unicastGoalState.getGoalStateBuilder().addDhcpStates(dhcpStateBuilder.build());
            }
        }
    }
}
