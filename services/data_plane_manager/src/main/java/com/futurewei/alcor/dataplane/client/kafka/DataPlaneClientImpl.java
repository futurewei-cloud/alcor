/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.client.kafka;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.HostGoalState;
import com.futurewei.alcor.schema.Goalstate;

import java.util.List;

public class DataPlaneClientImpl implements DataPlaneClient {
    @Override
    public void createGoalState(Goalstate.GoalState goalState, String hostIp) throws Exception {

    }

    @Override
    public void createGoalState(List<HostGoalState> hostGoalStates) throws Exception {

    }

    @Override
    public void updateGoalState(List<HostGoalState> hostGoalStates) throws Exception {

    }

    @Override
    public void deleteGoalState(List<HostGoalState> hostGoalStates) throws Exception {

    }
}
