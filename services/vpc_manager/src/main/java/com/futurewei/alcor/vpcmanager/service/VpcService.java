package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.web.entity.gateway.VpcInfo;
import com.futurewei.alcor.web.entity.gateway.VpcInfoJson;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcEntity vpcState);

    public VpcEntity allocateSegmentForNetwork (VpcEntity vpcEntity) throws Exception;

    public boolean checkSubnetsAreEmpty (VpcEntity vpcEntity) throws SubnetsNotEmptyException;

    public ResponseId registerVpc(VpcEntity vpcEntity);

}
