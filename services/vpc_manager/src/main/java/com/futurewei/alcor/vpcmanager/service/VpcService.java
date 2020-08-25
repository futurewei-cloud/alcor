package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RouterWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcEntity vpcState);

    public VpcEntity allocateSegmentForNetwork (VpcEntity vpcEntity) throws Exception;

    public boolean checkSubnetsAreEmpty (VpcEntity vpcEntity) throws SubnetsNotEmptyException;

}
