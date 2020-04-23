package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.*;

import java.util.concurrent.atomic.AtomicReference;

public interface SubnetService {

    // Subnet Route info Fallback
    public void routeFallback (String routeId, String vpcId);

    // Mac info Fallback
    public void macFallback (String macAddress);

    // Ip gateway Fallback
    public void ipFallback (int ipVersion, String rangeId, String ipAddr);

    // Fallback operation
    public void fallbackOperation (AtomicReference<RouteWebJson> routeResponseAtomic,
                                   AtomicReference<MacStateJson> macResponseAtomic,
                                   AtomicReference<IpAddrRequest> ipResponseAtomic,
                                   SubnetStateJson resource,
                                   String message);

    // Verify VPC ID
    public VpcStateJson verifyVpcId (String projectId, String vpcId) throws Exception;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson createRouteRules (String subnetId, SubnetState subnetState) throws Exception;

    // Allocate Gateway Mac
    public MacStateJson allocateMacGateway (String projectId, String vpcId, String portId) throws Exception;

    // TODO : Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
    public IpAddrRequest allocateIPGateway (String subnetId) throws Exception;

}
