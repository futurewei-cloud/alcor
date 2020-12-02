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
package com.futurewei.alcor.dataplane.client;

import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface DataPlaneClient {
    List<String> createGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception;
    List<String> createGoalStates(List<UnicastGoalState> unicastGoalStates,
                                  MulticastGoalState multicastGoalState) throws Exception;
}
