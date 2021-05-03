package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.schema.Goalstateprovisioner;

import java.util.List;

public interface OnDemandService {

    /**
     * Retrieve Resource Goal State according to the received resource state request.
     * @param resourceStateRequest
     * @return HostGoalState
     * @throws Exception Various exceptions that may occur during the create process
     */
    HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest);

    List<ResourceMeta> retrieveResourceMeta(String vni, String privateIp);

    List<Object> retrieveResourceState(List<String> resourceIds);
}
