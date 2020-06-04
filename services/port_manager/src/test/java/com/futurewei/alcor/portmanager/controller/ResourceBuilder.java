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
package com.futurewei.alcor.portmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrState;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.AllowAddressPair;
import com.futurewei.alcor.web.entity.port.FixedIp;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.RouteTableType;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;

import java.util.ArrayList;
import java.util.List;

public class ResourceBuilder {
    public static PortWebJson newPortStateJson(String portId) {
        List<FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(new FixedIp(UnitTestConfig.subnetId, UnitTestConfig.ip1));

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroupId1);

        List<AllowAddressPair> allowedAddressPairs = new ArrayList<>();
        allowedAddressPairs.add(new AllowAddressPair(UnitTestConfig.ip2, UnitTestConfig.mac1));

        PortEntity portEntity = new PortEntity();
        portEntity.setId(portId);
        portEntity.setName(UnitTestConfig.portName1);
        portEntity.setVpcId(UnitTestConfig.vpcId);
        portEntity.setProjectId(UnitTestConfig.projectId);
        portEntity.setTenantId(UnitTestConfig.tenantId);
        portEntity.setFixedIps(fixedIps);
        portEntity.setMacAddress(UnitTestConfig.mac1);
        portEntity.setBindingHostId(UnitTestConfig.nodeId1);
        portEntity.setSecurityGroups(securityGroups);
        portEntity.setAllowedAddressPairs(allowedAddressPairs);

        return new PortWebJson(portEntity);
    }

    public static IpAddrRequest newIpv4AddrRequest() {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setSubnetId(UnitTestConfig.subnetId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv4Version);
        ipAddrRequest.setIp(UnitTestConfig.ip1);
        ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());

        return ipAddrRequest;
    }

    public static IpAddrRequest newIpv6AddrRequest() {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv6Version);
        ipAddrRequest.setIp(UnitTestConfig.ipv6Address);
        ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());

        return ipAddrRequest;
    }

    public static String newPortStateJsonStr() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(newPortStateJson(UnitTestConfig.portId1));
    }

    public static VpcWebJson newVpcStateJson() {
        VpcEntity vpcState = new VpcEntity();
        vpcState.setId(UnitTestConfig.vpcId);
        vpcState.setProjectId(UnitTestConfig.projectId);
        vpcState.setCidr(UnitTestConfig.vpcCidr);

        return new VpcWebJson(vpcState);
    }

    public static SubnetWebJson newSubnetStateJson() {
        SubnetEntity subnetState = new SubnetEntity();
        subnetState.setProjectId(UnitTestConfig.projectId);
        subnetState.setId(UnitTestConfig.subnetId);
        subnetState.setName("subnet1");
        subnetState.setCidr(UnitTestConfig.vpcCidr);
        subnetState.setVpcId(UnitTestConfig.vpcId);
        subnetState.setIpV4RangeId(UnitTestConfig.rangeId);
        subnetState.setGatewayIp(UnitTestConfig.ip1);
        subnetState.setGatewayMacAddress(UnitTestConfig.mac1);

        return new SubnetWebJson(subnetState);
    }

    public static MacStateJson newMacStateJson(String portId, String macAddress) {
        MacState macState = new MacState();
        macState.setProjectId(UnitTestConfig.projectId);
        macState.setVpcId(UnitTestConfig.vpcId);
        macState.setPortId(portId);
        macState.setMacAddress(macAddress);

        return new MacStateJson(macState);
    }

    public static NodeInfoJson newNodeInfoJson(String nodeId, String ipAddress) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(nodeId);
        nodeInfo.setLocalIp(ipAddress);
        nodeInfo.setMacAddress(UnitTestConfig.mac1);

        return new NodeInfoJson(nodeInfo);
    }

    public static RouteWebJson newRouteWebJson() {
        RouteEntity route = new RouteEntity();
        route.setDestination(UnitTestConfig.routeDestination);
        route.setTarget(UnitTestConfig.routeTarget);
        route.setAssociatedType(RouteTableType.MAIN);

        return new RouteWebJson(route);
    }

    public static SecurityGroupJson newSecurityGroupWebJson(String securityGroupId) {
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setId(securityGroupId);
        securityGroup.setName(UnitTestConfig.securityGroupName);
        securityGroup.setTenantId(UnitTestConfig.tenantId);
        securityGroup.setProjectId(UnitTestConfig.projectId);

        SecurityGroupRule securityGroupRule = new SecurityGroupRule();
        securityGroupRule.setId(UnitTestConfig.securityGroupRuleId);
        securityGroupRule.setProjectId(UnitTestConfig.projectId);
        securityGroupRule.setTenantId(UnitTestConfig.tenantId);
        securityGroupRule.setSecurityGroupId(UnitTestConfig.securityGroupId1);
        securityGroupRule.setDirection(UnitTestConfig.direction1);
        securityGroupRule.setProtocol(UnitTestConfig.protocolTcp);
        securityGroupRule.setPortRangeMin(UnitTestConfig.portRangeMin);
        securityGroupRule.setPortRangeMax(UnitTestConfig.portRangeMax);
        securityGroupRule.setEtherType(UnitTestConfig.etherType);

        List<SecurityGroupRule> securityGroupRuleEntities = new ArrayList<>();
        securityGroupRuleEntities.add(securityGroupRule);
        securityGroup.setSecurityGroupRules(securityGroupRuleEntities);

        return new SecurityGroupJson(securityGroup);
    }

    public static SecurityGroupJson newDefaultSecurityGroupWebJson() {
        SecurityGroupJson securityGroupJson = newSecurityGroupWebJson(UnitTestConfig.securityGroupId1);
        securityGroupJson.getSecurityGroup().setName("default");

        return securityGroupJson;
    }
}
