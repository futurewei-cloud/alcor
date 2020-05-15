package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebResponseObject;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcWebResponseObject vpcState);

}
