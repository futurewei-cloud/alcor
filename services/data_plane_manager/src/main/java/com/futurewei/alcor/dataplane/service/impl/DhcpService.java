/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
                dhcpConfigBuilder.setSubnetId(fixedIp.getSubnetId());

                //TODO: support ipv6
                //dhcpConfigBuilder.setIpv6Address();
                //dhcpConfigBuilder.setPortHostName();
                //dhcpConfigBuilder.setExtraDhcpOptions();
                //dhcpConfigBuilder.setDnsEntryList();

                DHCP.DHCPState.Builder dhcpStateBuilder = DHCP.DHCPState.newBuilder();
                dhcpStateBuilder.setOperationType(portState.getOperationType());
                dhcpStateBuilder.setConfiguration(dhcpConfigBuilder.build());
                unicastGoalState.getGoalStateBuilder().addDhcpStates(dhcpStateBuilder.build());
            }
        }
    }
}
