package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.*;

import com.futurewei.alcor.subnet.exception.*;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.subnet.NewHostRoutes;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebRequestJson;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.atomic.AtomicReference;

public interface SubnetService {

    // Subnet Route info Fallback
    public void routeFallback (String routeId, String vpcId);

    // Mac info Fallback
    public void macFallback (String macAddress);

    // Ip gateway Fallback
    public void ipFallback (String rangeId, String ipAddr);

    // Fallback operation
    public void fallbackOperation (AtomicReference<RouteWebJson> routeResponseAtomic,
                                   AtomicReference<MacStateJson> macResponseAtomic,
                                   AtomicReference<IpAddrRequest> ipResponseAtomic,
                                   SubnetWebRequestJson resource,
                                   String message) throws CacheException;

    // Verify VPC ID
    public VpcWebJson verifyVpcId (String projectId, String vpcId) throws Exception;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson createRouteRules (String subnetId, SubnetEntity subnetEntity) throws Exception;

    // Allocate Gateway Mac
    public MacStateJson allocateMacAddressForGatewayPort(String projectId, String vpcId, String portId) throws Exception;

    // Verify/Allocate Gateway IP
    public IpAddrRequest allocateIpAddressForGatewayPort(String subnetId, String cidr, String vpcId, String gatewayIp, boolean isOpenToBeAllocated) throws Exception;

    // Transfer cidr to first IP and last IP
    public String[] cidrToFirstIpAndLastIp (String cidr);

    // Verify cidr block
    public boolean verifyCidrBlock (String cidr) throws ParameterUnexpectedValueException, FallbackException;

    // Get used_ips for ip range
    public Integer getUsedIpByRangeId (String rangeId) throws UsedIpsIsNotCorrect;

    // update to vpc with subnet id
    public void addSubnetIdToVpc (String subnetId, String projectId, String vpcId) throws Exception;

    // delete subnet id in vpc
    public void deleteSubnetIdInVpc (String subnetId, String projectId, String vpcId) throws Exception;

    // check if there is any port in this subnet
    public boolean checkIfAnyPortInSubnet (String rangeId) throws RangeIdIsNullOrEmpty;

    // check if subnet bind any routes
    public boolean checkIfSubnetBindAnyRoutes (SubnetEntity subnetEntity);

    // check if cidr overlap
    public boolean checkIfCidrOverlap (String cidr,String projectId, String vpcId) throws FallbackException, ResourceNotFoundException, ResourcePersistenceException, CidrNotWithinNetworkCidr, CidrOverlapWithOtherSubnets;

    // update subnet host routes in subnet manager
    public void updateSubnetHostRoutes (String subnetId, NewHostRoutes resource) throws ResourceNotFoundException, ResourcePersistenceException, DatabasePersistenceException, SubnetEntityNotFound, DestinationOrOperationTypeIsNull;

    // delete subnet routing rule in route manager
    public void deleteSubnetRoutingRuleInRM (String projectId, String subnetId) throws SubnetIdIsNull;

    // update subnet routing rule in route manager
    public void updateSubnetRoutingRuleInRM (String projectId, String subnetId, SubnetEntity subnetEntity) throws SubnetIdIsNull;

    // create subnet routing rule in route manager
    public void createSubnetRoutingRuleInRM (String projectId, String subnetId, SubnetEntity subnetEntity) throws SubnetIdIsNull;

    // construct port entity passed in PM
    public PortEntity constructPortEntity (String portId, String vpcId, String subnetId, String gatewayIP, String deviceOwner);

    //Delete ip range from subnet
    void deleteIpRange(String projectId, String rangeId);
}
