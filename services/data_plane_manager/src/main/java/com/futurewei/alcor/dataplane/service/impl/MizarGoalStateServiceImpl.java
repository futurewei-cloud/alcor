package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;

public class MizarGoalStateServiceImpl implements GoalStateService {
    @Override
    public Goalstateprovisioner.GoalStateOperationReply SendGoalStateToHosts() {
        return null;
    }

    @Override
    public void setGoalState(Goalstate.GoalState goalState) {

    }

    @Override
    public void setIp(String ip) {

    }

    @Override
    public void setFastPath(boolean fastPath) {

    }
}
