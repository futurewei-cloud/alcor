package com.futurewei.alcor.subnet.service;

import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.subnet.entity.RouteWebJson;
import com.futurewei.alcor.subnet.entity.SubnetState;
import com.futurewei.alcor.subnet.entity.VpcStateJson;

public interface SubnetService {

    // Verify VPC ID
    public VpcStateJson verifyVpcId (String projectid, SubnetState inSubnetState) throws ResourcePersistenceException;

    // Prepare Route Rule(IPv4/6) for Subnet
    public RouteWebJson prepeareRouteRule (SubnetState inSubnetState, VpcStateJson vpcResponse) throws ResourcePersistenceException;

    // TODO : Allocate Gateway Mac

    // TODO : Verify/Allocate Gateway IP, subnet id, port id, subnet cidr, response:IP - unique

}
