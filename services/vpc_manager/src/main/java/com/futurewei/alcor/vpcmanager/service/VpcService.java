package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.vpcmanager.entity.RouteWebJson;
import com.futurewei.alcor.vpcmanager.entity.VpcState;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcState vpcState);

}
