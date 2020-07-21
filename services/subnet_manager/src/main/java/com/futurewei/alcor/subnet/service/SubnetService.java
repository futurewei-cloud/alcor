package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.FallbackException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;

import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetRequestWebJson;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;

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
                                   SubnetRequestWebJson resource,
                                   String message) throws CacheException;

    // Verify VPC ID
    public VpcWebJson verifyVpcId (String projectId, String vpcId) throws Exception;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson createRouteRules (String subnetId, SubnetEntity subnetEntity) throws Exception;

    // Allocate Gateway Mac
    public MacStateJson allocateMacAddressForGatewayPort(String projectId, String vpcId, String portId) throws Exception;

    // Verify/Allocate Gateway IP
    public IpAddrRequest allocateIpAddressForGatewayPort(String subnetId, String cidr, String vpcId) throws Exception;

    // Transfer cidr to first IP and last IP
    public String[] cidrToFirstIpAndLastIp (String cidr);

    // Verify cidr block
    public boolean verifyCidrBlock (String cidr) throws ParameterUnexpectedValueException, FallbackException;

    // update to vpc with subnet id
    public void addSubnetIdToVpc (String subnetId, String projectId, String vpcId) throws Exception;

}
