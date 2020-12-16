package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.io.IOException;
import java.util.Map;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcEntity vpcState, Map<String,String> httpHeaders) throws IOException;

    public VpcEntity allocateSegmentForNetwork (VpcEntity vpcEntity) throws Exception;

    public boolean checkSubnetsAreEmpty (VpcEntity vpcEntity) throws SubnetsNotEmptyException;

}
