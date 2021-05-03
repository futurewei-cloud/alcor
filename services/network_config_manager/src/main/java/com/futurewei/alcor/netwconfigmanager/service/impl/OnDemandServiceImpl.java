package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.service.OnDemandService;
import com.futurewei.alcor.schema.Goalstateprovisioner;

public class OnDemandServiceImpl implements OnDemandService {

    @Override
    public HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest) {
        return null;
    }
}
