package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.*;

public interface SubnetService {

    // Subnet Route info Rollback
    public void routeRollback (String routeId, String vpcId);

    // Verify VPC ID
    public VpcStateJson verifyVpcId (String projectId, String vpcId) throws Exception;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson createRouteRules (String subnetId, SubnetState subnetState) throws Exception;

    // TODO : Allocate Gateway Mac
    public MacStateJson allocateMacGateway (String projectId, String vpcId, String portId);

    // TODO : Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique
    public IPStateJson allocateIPGateway (String subnetId, String cidr, String portId);

}
