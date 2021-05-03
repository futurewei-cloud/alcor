package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.schema.Goalstateprovisioner;

public interface OnDemandService {

    HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest);
}
