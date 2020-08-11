package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.exception.CidrNotWithinNetworkCidr;
import com.futurewei.alcor.subnet.exception.CidrOverlapWithOtherSubnets;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebRequestJson;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;

import java.util.concurrent.atomic.AtomicReference;

public interface SubnetService {

    // VPC Fallback
    public void vpcFallback (String projectId, String vpcId, String subnetId);

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
    public  Integer getUsedIpByRangeId (String rangeId);

    // update to vpc with subnet id
    public void addSubnetIdToVpc (String subnetId, String projectId, String vpcId) throws Exception;

    // delete subnet id in vpc
    public void deleteSubnetIdInVpc (String subnetId, String projectId, String vpcId) throws Exception;

    // check if cidr overlap
    public boolean checkIfCidrOverlap (String cidr,String projectId, String vpcId) throws FallbackException, ResourceNotFoundException, ResourcePersistenceException, CidrNotWithinNetworkCidr, CidrOverlapWithOtherSubnets;
}
