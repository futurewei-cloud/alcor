package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.RouteWebJson;
import com.futurewei.alcor.subnet.entity.SubnetState;
import com.futurewei.alcor.subnet.entity.VpcStateJson;

public interface SubnetService {

    // Subnet Route info Rollback
    public void routeRollback (String routeId, String vpcId);

    // Verify VPC ID
    public VpcStateJson verifyVpcId (String projectid, String vpcId) throws ResourcePersistenceException;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson prepeareRouteRule (String vpcId, VpcStateJson vpcResponse) throws ResourcePersistenceException;

    // TODO : Allocate Gateway Mac

    // TODO : Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique

}
