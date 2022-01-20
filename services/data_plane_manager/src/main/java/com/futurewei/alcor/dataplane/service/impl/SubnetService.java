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

import com.futurewei.alcor.dataplane.cache.SubnetPortsCache;
import com.futurewei.alcor.dataplane.cache.SubnetPortsCacheV2;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.MulticastGoalStateV2;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.dataplane.exception.SubnetEntityNotFound;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SubnetService extends ResourceService {

    @Autowired
    private SubnetPortsCacheV2 subnetPortsCacheV2;

    @Autowired
    private SubnetPortsCache subnetPortsCache;

    public InternalSubnetEntity getInternalSubnetEntity(NetworkConfiguration networkConfig, String subnetId) throws Exception {
        InternalSubnetEntity result = null;
        for (InternalSubnetEntity internalSubnetEntity: networkConfig.getSubnets()) {
            if (internalSubnetEntity.getId().equals(subnetId)) {
                result = internalSubnetEntity;
            }
        }

        if (result == null) {
            throw new SubnetEntityNotFound();
        }

        return result;
    }

    public void buildSubnetStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState, MulticastGoalState multicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            for (Port.PortConfiguration.FixedIp fixedIp: portState.getConfiguration().getFixedIpsList()) {
                InternalSubnetEntity internalSubnetEntity = getInternalSubnetEntity(
                        networkConfig, fixedIp.getSubnetId());
                subnetEntities.add(internalSubnetEntity);
            }
        }

        for (InternalSubnetEntity subnetEntity: subnetEntities) {
            // check if subnet state already exists in the unicastGoalState
            if (unicastGoalState.getGoalStateBuilder().getSubnetStatesList().stream()
                    .filter(e -> e.getConfiguration().getId().equals(subnetEntity.getId()))
                    .findFirst().orElse(null) != null) {
                continue;
            }

            buildSubnetState(
                    subnetEntity.getId(),
                    subnetEntity.getVpcId(),
                    subnetEntity.getName(),
                    subnetEntity.getCidr(),
                    subnetEntity.getTunnelId(),
                    subnetEntity.getGatewayIp(),
                    subnetEntity.getGatewayPortDetail().getGatewayMacAddress(),
                    subnetEntity.getDhcpEnable(),
                    subnetEntity.getAvailabilityZone(),
                    unicastGoalState,
                    multicastGoalState
            );
        }
    }

    public void buildSubnetState (String subnetId, UnicastGoalState unicastGoalState, MulticastGoalState multicastGoalState) throws Exception
    {
        InternalSubnetPorts subnetEntity = subnetPortsCache.getSubnetPorts(subnetId);
        if (subnetEntity != null) {
            if (unicastGoalState.getGoalStateBuilder().getSubnetStatesList().stream()
                    .filter(e -> e.getConfiguration().getId().equals(subnetEntity.getSubnetId()))
                    .findFirst().orElse(null) == null)
            {
                buildSubnetState(
                        subnetEntity.getSubnetId(),
                        subnetEntity.getVpcId(),
                        subnetEntity.getName(),
                        subnetEntity.getCidr(),
                        subnetEntity.getTunnelId(),
                        subnetEntity.getGatewayPortIp(),
                        subnetEntity.getGatewayPortMac(),
                        subnetEntity.getDhcpEnable(),
                        null,
                        unicastGoalState,
                        multicastGoalState
                        );
            }
        }
    }

    public Subnet.SubnetState.Builder buildSubnetState (String subnetId) throws Exception
    {
        InternalSubnetPorts subnetEntity = subnetPortsCacheV2.getSubnetPorts(subnetId);
        Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
        subnetConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
        subnetConfigBuilder.setId(subnetId);
        subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
        subnetConfigBuilder.setVpcId(subnetEntity.getVpcId());
        subnetConfigBuilder.setName(subnetEntity.getName());
        subnetConfigBuilder.setCidr(subnetEntity.getCidr());
        if (subnetEntity.getTunnelId() != null) {
            subnetConfigBuilder.setTunnelId(subnetEntity.getTunnelId());
        }
        Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
        gatewayBuilder.setIpAddress(subnetEntity.getGatewayPortIp());
        gatewayBuilder.setMacAddress(subnetEntity.getGatewayPortMac());
        subnetConfigBuilder.setGateway(gatewayBuilder.build());

        if (subnetEntity.getDhcpEnable() != null) {
            subnetConfigBuilder.setDhcpEnable(true);
        }

        // TODO: need to set DNS based on latest contract

        Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
        subnetStateBuilder.setOperationType(Common.OperationType.INFO);
        subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
        return subnetStateBuilder;
    }

    public void buildSubnetState (String subnetId, UnicastGoalStateV2 unicastGoalState, MulticastGoalStateV2 multicastGoalState) throws Exception
    {
        InternalSubnetPorts subnetEntity = subnetPortsCacheV2.getSubnetPorts(subnetId);
        if (subnetEntity != null) {
            if (unicastGoalState.getGoalStateBuilder().getSubnetStatesMap().entrySet().stream()
                    .filter(e -> e.getKey().equals(subnetEntity.getSubnetId()))
                    .findFirst().orElse(null) == null)
            {
                buildSubnetState(
                        subnetEntity.getSubnetId(),
                        subnetEntity.getVpcId(),
                        subnetEntity.getName(),
                        subnetEntity.getCidr(),
                        subnetEntity.getTunnelId(),
                        subnetEntity.getGatewayPortIp(),
                        subnetEntity.getGatewayPortMac(),
                        subnetEntity.getDhcpEnable(),
                        null,
                        unicastGoalState,
                        multicastGoalState
                );
            }
        }
    }

    private void buildSubnetState(String id,
                                  String vpcId,
                                  String name,
                                  String cidr,
                                  long tunnelId,
                                  String gatewayIp,
                                  String gatewayMac,
                                  Boolean enableDhcp,
                                  String availabilityZone,
                                  UnicastGoalState unicastGoalState,
                                  MulticastGoalState multicastGoalState)
    {
        Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
        subnetConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
        subnetConfigBuilder.setId(id);
        subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
        subnetConfigBuilder.setVpcId(vpcId);
        subnetConfigBuilder.setName(name);
        subnetConfigBuilder.setCidr(cidr);
        subnetConfigBuilder.setTunnelId(tunnelId);

        Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
        gatewayBuilder.setIpAddress(gatewayIp);
        gatewayBuilder.setMacAddress(gatewayMac);
        subnetConfigBuilder.setGateway(gatewayBuilder.build());

        if (enableDhcp != null) {
            subnetConfigBuilder.setDhcpEnable(enableDhcp);
        }

        // TODO: need to set DNS based on latest contract

        if (availabilityZone != null) {
            subnetConfigBuilder.setAvailabilityZone(availabilityZone);
        }

        Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
        subnetStateBuilder.setOperationType(Common.OperationType.INFO);
        subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
        unicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
        multicastGoalState.getGoalStateBuilder().addSubnetStates(subnetStateBuilder.build());
    }

    private void buildSubnetState(String id,
                                  String vpcId,
                                  String name,
                                  String cidr,
                                  long tunnelId,
                                  String gatewayIp,
                                  String gatewayMac,
                                  Boolean enableDhcp,
                                  String availabilityZone,
                                  UnicastGoalStateV2 unicastGoalState,
                                  MulticastGoalStateV2 multicastGoalState)
    {
        Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
        subnetConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
        subnetConfigBuilder.setId(id);
        subnetConfigBuilder.setNetworkType(Common.NetworkType.VXLAN);
        subnetConfigBuilder.setVpcId(vpcId);
        subnetConfigBuilder.setName(name);
        subnetConfigBuilder.setCidr(cidr);
        subnetConfigBuilder.setTunnelId(tunnelId);

        Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
        gatewayBuilder.setIpAddress(gatewayIp);
        gatewayBuilder.setMacAddress(gatewayMac);
        subnetConfigBuilder.setGateway(gatewayBuilder.build());

        if (enableDhcp != null) {
            subnetConfigBuilder.setDhcpEnable(enableDhcp);
        }

        // TODO: need to set DNS based on latest contract

        if (availabilityZone != null) {
            subnetConfigBuilder.setAvailabilityZone(availabilityZone);
        }

        Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
        subnetStateBuilder.setOperationType(Common.OperationType.INFO);
        subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
        if (unicastGoalState != null) {
            unicastGoalState.getGoalStateBuilder().putSubnetStates(id, subnetStateBuilder.build());
        }
        if (multicastGoalState != null) {
            multicastGoalState.getGoalStateBuilder().putSubnetStates(id, subnetStateBuilder.build());
        }

    }

    public void buildSubnetStates(NetworkConfiguration networkConfig, UnicastGoalStateV2 unicastGoalState) throws Exception {
        Map<String, Port.PortState> portStateMap = unicastGoalState.getGoalStateBuilder().getPortStatesMap();
        List<Port.PortState> portStates = new ArrayList<Port.PortState>(portStateMap.values());
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        List<InternalSubnetEntity> subnetEntities = new ArrayList<>();
        for (Port.PortState portState: portStates) {
            for (Port.PortConfiguration.FixedIp fixedIp: portState.getConfiguration().getFixedIpsList()) {
                InternalSubnetEntity internalSubnetEntity = getInternalSubnetEntity(
                        networkConfig, fixedIp.getSubnetId());
                subnetEntities.add(internalSubnetEntity);
            }
        }

        for (InternalSubnetEntity subnetEntity: subnetEntities) {
            // check if subnet state already exists in the unicastGoalState
            if (unicastGoalState.getGoalStateBuilder().getSubnetStatesMap().containsKey(subnetEntity.getId())) {
                continue;
            }
            Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
            subnetConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER)
                    .setId(subnetEntity.getId())
                    .setNetworkType(Common.NetworkType.VXLAN)
                    .setVpcId(subnetEntity.getVpcId())
                    .setName(subnetEntity.getName())
                    .setCidr(subnetEntity.getCidr())
                    .setTunnelId(subnetEntity.getTunnelId());

            Subnet.SubnetConfiguration.Gateway.Builder gatewayBuilder = Subnet.SubnetConfiguration.Gateway.newBuilder();
            gatewayBuilder.setIpAddress(subnetEntity.getGatewayIp());
            gatewayBuilder.setMacAddress(subnetEntity.getGatewayPortDetail().getGatewayMacAddress());
            subnetConfigBuilder.setGateway(gatewayBuilder.build());

            if (subnetEntity.getDhcpEnable() != null) {
                subnetConfigBuilder.setDhcpEnable(subnetEntity.getDhcpEnable());
            }

            // TODO: need to set DNS based on latest contract

            if (subnetEntity.getAvailabilityZone() != null) {
                subnetConfigBuilder.setAvailabilityZone(subnetEntity.getAvailabilityZone());
            }

            Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
            subnetStateBuilder.setOperationType(Common.OperationType.INFO);

            subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());
            Subnet.SubnetState subnetState = subnetStateBuilder.build();
            unicastGoalState.getGoalStateBuilder().putSubnetStates(subnetState.getConfiguration().getId(), subnetState);
        }
    }
}
