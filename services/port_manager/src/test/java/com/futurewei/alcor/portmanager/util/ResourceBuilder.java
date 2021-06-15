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
package com.futurewei.alcor.portmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.portmanager.config.UnitTestConfig;
import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.portmanager.entity.SubnetPortIds;
import com.futurewei.alcor.web.entity.ip.IpAddrUpdateRequest;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfoWrapper;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrRequestBulk;
import com.futurewei.alcor.web.entity.ip.IpAddrState;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;

import java.util.*;

public class ResourceBuilder {
    public static PortEntity buildPortEntity(String portId) {
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(new PortEntity.FixedIp(UnitTestConfig.subnetId, UnitTestConfig.ip1));

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(UnitTestConfig.securityGroupId1);

        List<PortEntity.AllowAddressPair> allowedAddressPairs = new ArrayList<>();
        allowedAddressPairs.add(new PortEntity.AllowAddressPair(UnitTestConfig.ip2, UnitTestConfig.mac1));

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

        return portEntity;
    }


    public static PortWebJson buildPortWebJson(String portId) {
        return new PortWebJson(buildPortEntity(portId));
    }

    public static IpAddrRequest buildIpv4AddrRequest(String ipAddress) {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setSubnetId(UnitTestConfig.subnetId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv4Version);
        ipAddrRequest.setVpcId(UnitTestConfig.vpcId);
        ipAddrRequest.setIp(ipAddress);
        ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());

        return ipAddrRequest;
    }

    public static IpAddrUpdateRequest buildIpAddrUpdateRequest(String ipAddress) {
        IpAddrUpdateRequest ipAddrUpdateRequest = new IpAddrUpdateRequest();
        IpAddrRequest ipAddrRequest = buildIpv4AddrRequest(ipAddress);
        ipAddrUpdateRequest.setOldIpAddrRequests(Collections.singletonList(ipAddrRequest));
        ipAddrUpdateRequest.setNewIpAddrRequests(Collections.singletonList(ipAddrRequest));

        return ipAddrUpdateRequest;
    }

    public static IpAddrRequestBulk buildIpAddrRequestBulk() {
        IpAddrRequest ipAddrRequest1 = new IpAddrRequest();
        ipAddrRequest1.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest1.setSubnetId(UnitTestConfig.subnetId);
        ipAddrRequest1.setIpVersion(UnitTestConfig.ipv4Version);
        ipAddrRequest1.setIp(UnitTestConfig.ip1);
        ipAddrRequest1.setState(IpAddrState.ACTIVATED.getState());

        IpAddrRequest ipAddrRequest2 = new IpAddrRequest();
        ipAddrRequest2.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest2.setSubnetId(UnitTestConfig.subnetId);
        ipAddrRequest2.setIpVersion(UnitTestConfig.ipv4Version);
        ipAddrRequest2.setIp(UnitTestConfig.ip2);
        ipAddrRequest2.setState(IpAddrState.ACTIVATED.getState());

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        ipAddrRequests.add(ipAddrRequest1);
        ipAddrRequests.add(ipAddrRequest2);

        return new IpAddrRequestBulk(ipAddrRequests);
    }

    public static IpAddrRequest buildIpv6AddrRequest() {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(UnitTestConfig.rangeId);
        ipAddrRequest.setIpVersion(UnitTestConfig.ipv6Version);
        ipAddrRequest.setIp(UnitTestConfig.ipv6Address);
        ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());

        return ipAddrRequest;
    }

    public static String buildPortStateJsonStr() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(buildPortWebJson(UnitTestConfig.portId1));
    }

    public static VpcWebJson buildVpcWebJson() {
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcEntity.setProjectId(UnitTestConfig.projectId);
        vpcEntity.setCidr(UnitTestConfig.vpcCidr);

        return new VpcWebJson(vpcEntity);
    }

    public static VpcsWebJson buildVpcsWebJson() {
        VpcEntity vpcEntity = new VpcEntity();
        vpcEntity.setId(UnitTestConfig.vpcId);
        vpcEntity.setProjectId(UnitTestConfig.projectId);
        vpcEntity.setCidr(UnitTestConfig.vpcCidr);

        return new VpcsWebJson(Collections.singletonList(vpcEntity));
    }

    public static SubnetWebJson buildSubnetWebJson() {
        SubnetEntity subnetEntity = new SubnetEntity();
        subnetEntity.setProjectId(UnitTestConfig.projectId);
        subnetEntity.setId(UnitTestConfig.subnetId);
        subnetEntity.setName("subnet1");
        subnetEntity.setCidr(UnitTestConfig.vpcCidr);
        subnetEntity.setVpcId(UnitTestConfig.vpcId);
        subnetEntity.setIpV4RangeId(UnitTestConfig.rangeId);
        subnetEntity.setGatewayIp(UnitTestConfig.ip1);
        subnetEntity.setGatewayPortDetail(new GatewayPortDetail(UnitTestConfig.mac1, UnitTestConfig.portId1));

        return new SubnetWebJson(subnetEntity);
    }

    public static SubnetsWebJson buildSubnetsWebJson() {
        SubnetEntity subnetEntity = new SubnetEntity();
        subnetEntity.setProjectId(UnitTestConfig.projectId);
        subnetEntity.setId(UnitTestConfig.subnetId);
        subnetEntity.setName("subnet1");
        subnetEntity.setCidr(UnitTestConfig.vpcCidr);
        subnetEntity.setVpcId(UnitTestConfig.vpcId);
        subnetEntity.setIpV4RangeId(UnitTestConfig.rangeId);
        subnetEntity.setGatewayIp(UnitTestConfig.ip1);
        subnetEntity.setGatewayPortDetail(new GatewayPortDetail(UnitTestConfig.mac1, UnitTestConfig.portId1));

        return new SubnetsWebJson(Collections.singletonList(subnetEntity));
    }

    public static MacStateJson buildMacStateJson(String portId, String macAddress) {
        MacState macState = new MacState();
        macState.setProjectId(UnitTestConfig.projectId);
        macState.setVpcId(UnitTestConfig.vpcId);
        macState.setPortId(portId);
        macState.setMacAddress(macAddress);

        return new MacStateJson(macState);
    }

    public static MacStateBulkJson buildMacStateBulkJson(String portId) {
        MacState macState1 = new MacState();
        macState1.setProjectId(UnitTestConfig.projectId);
        macState1.setVpcId(UnitTestConfig.vpcId);
        macState1.setPortId(portId);
        macState1.setMacAddress(UnitTestConfig.mac1);

        MacState macState2 = new MacState();
        macState2.setProjectId(UnitTestConfig.projectId);
        macState2.setVpcId(UnitTestConfig.vpcId);
        macState2.setPortId(portId);
        macState2.setMacAddress(UnitTestConfig.mac2);

        List<MacState> macStates = new ArrayList<>();
        macStates.add(macState1);
        macStates.add(macState2);

        return new MacStateBulkJson(macStates);
    }

    public static NodeInfoJson buildNodeInfoJson(String nodeId, String ipAddress) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(nodeId);
        nodeInfo.setName(nodeId);
        nodeInfo.setLocalIp(ipAddress);
        nodeInfo.setMacAddress(UnitTestConfig.mac1);

        return new NodeInfoJson(nodeInfo);
    }

    public static RouteWebJson buildRouteWebJson() {
        RouteEntity route = new RouteEntity();
        route.setDestination(UnitTestConfig.routeDestination);
        route.setTarget(UnitTestConfig.routeTarget);
        route.setAssociatedType(RouteTableType.VPC);

        return new RouteWebJson(route);
    }

    public static RoutesWebJson buildRoutesWebJson() {
        RouteEntity route = new RouteEntity();
        route.setDestination(UnitTestConfig.routeDestination);
        route.setTarget(UnitTestConfig.routeTarget);
        route.setAssociatedType(RouteTableType.VPC);

        List<RouteEntity> routes = new ArrayList<>();
        routes.add(route);

        return new RoutesWebJson(routes);
    }

    public static SecurityGroupJson buildSecurityGroupWebJson(String securityGroupId) {
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

    public static SecurityGroupsJson buildSecurityGroupsJson(String securityGroupId) {
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

        return new SecurityGroupsJson(Collections.singletonList(securityGroup));
    }

    public static SecurityGroupJson buildDefaultSecurityGroupWebJson() {
        SecurityGroupJson securityGroupJson = buildSecurityGroupWebJson(UnitTestConfig.securityGroupId1);
        securityGroupJson.getSecurityGroup().setName("default");

        return securityGroupJson;
    }

    public static NeighborInfo buildNeighborInfo(String portId) {
        NeighborInfo neighborInfo = new NeighborInfo(UnitTestConfig.ip1,
                UnitTestConfig.nodeId1, portId, UnitTestConfig.mac1,
                UnitTestConfig.ip1, UnitTestConfig.vpcId, UnitTestConfig.subnetId);

        return neighborInfo;
    }

    public static PortNeighbors buildPortNeighbors(String portId) {
        NeighborInfo neighborInfo = buildNeighborInfo(portId);

        Map<String, NeighborInfo> neighborInfoMap = new HashMap<>();
        neighborInfoMap.put(portId, neighborInfo);

        return new PortNeighbors(UnitTestConfig.vpcId, neighborInfoMap);
    }

    public static Map<String, NeighborInfo> buildNeighbors() {
        Map<String, NeighborInfo> neighborInfoMap = new HashMap<>();
        NeighborInfo neighborInfo1 = new NeighborInfo(UnitTestConfig.ip1,
                UnitTestConfig.nodeId1, UnitTestConfig.portId1, UnitTestConfig.mac1,
                UnitTestConfig.ip1, UnitTestConfig.vpcId, UnitTestConfig.subnetId);
        NeighborInfo neighborInfo2 = new NeighborInfo(UnitTestConfig.ip2,
                UnitTestConfig.nodeId2, UnitTestConfig.portId2, UnitTestConfig.mac2,
                UnitTestConfig.ip2, UnitTestConfig.vpcId, UnitTestConfig.subnetId2);
        neighborInfoMap.put(UnitTestConfig.portId1, neighborInfo1);
        neighborInfoMap.put(UnitTestConfig.portId2, neighborInfo2);

        return neighborInfoMap;
    }


    public static ElasticIpInfoWrapper buildElasticIp() {
        ElasticIpInfo elasticIpInfo = new ElasticIpInfo();
        elasticIpInfo.setId(UnitTestConfig.elasticIpId1);
        elasticIpInfo.setTenantId(UnitTestConfig.tenantId);
        elasticIpInfo.setElasticIpVersion(4);
        elasticIpInfo.setElasticIp(UnitTestConfig.elasticIpAddress1);
        elasticIpInfo.setPortId("");

        return new ElasticIpInfoWrapper(elasticIpInfo);
    }

    public static ConnectedSubnetsWebResponse buildRouterSubnets() {
        List<SubnetEntity> subnetEntities = new ArrayList<>();
        SubnetEntity subnetEntity1 = new SubnetEntity(UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.subnetId, "Subnet1", UnitTestConfig.subnet1Cidr);
        SubnetEntity subnetEntity2 = new SubnetEntity(UnitTestConfig.projectId, UnitTestConfig.vpcId, UnitTestConfig.subnetId2, "Subnet2", UnitTestConfig.subnet2Cidr);
        subnetEntities.add(subnetEntity1);
        subnetEntities.add(subnetEntity1);

        InternalRouterInfo router = new InternalRouterInfo();
        ConnectedSubnetsWebResponse routerSubnets = new ConnectedSubnetsWebResponse(router, subnetEntities);
        return routerSubnets;
    }

    public static int buildSubnetPorts() {
        SubnetPortIds subnetPortIds = new SubnetPortIds();
        subnetPortIds.setSubnetId(UnitTestConfig.subnetId);

        Set<String> portIds = new HashSet<>();
        portIds.add(UnitTestConfig.portId1);
        portIds.add(UnitTestConfig.portId2);
        subnetPortIds.setPortIds(portIds);

        return subnetPortIds.getPortIds().size();
    }
}
