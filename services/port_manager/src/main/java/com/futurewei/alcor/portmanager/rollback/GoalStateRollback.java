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
package com.futurewei.alcor.portmanager.rollback;

import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import java.util.ArrayList;
import java.util.List;

public abstract class GoalStateRollback implements Rollback {
    protected DataPlaneManagerRestClient dataPlaneManagerRestClient;

    protected List<Goalstate.GoalState> createdGoalStates = new ArrayList<>();
    protected List<Goalstate.GoalState> deletedGoalStates = new ArrayList<>();
    protected List<Goalstate.GoalState> updatedGoalStates = new ArrayList<>();

    public GoalStateRollback(DataPlaneManagerRestClient dataPlaneManagerRestClient) {
        this.dataPlaneManagerRestClient = dataPlaneManagerRestClient;
    }

    public abstract void doRollback() throws Exception;

    public void createGoalState(Goalstate.GoalState goalState) {
        createdGoalStates.add(goalState);
    }

    public void deleteGoalState(Goalstate.GoalState goalState) {
        deletedGoalStates.add(goalState);
    }

    public void updateGoalState(Goalstate.GoalState goalState) {
        updatedGoalStates.add(goalState);
    }
}
