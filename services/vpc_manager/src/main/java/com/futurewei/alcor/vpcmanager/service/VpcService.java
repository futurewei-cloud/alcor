package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcEntity vpcState);

    public VpcEntity allocateSegmentForNetwork (VpcEntity vpcEntity) throws Exception;

}
