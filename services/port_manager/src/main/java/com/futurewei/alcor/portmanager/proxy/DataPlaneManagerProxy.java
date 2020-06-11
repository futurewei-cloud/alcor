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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.rollback.CreateGoalStateRollback;
import com.futurewei.alcor.portmanager.rollback.DeleteGoalStateRollback;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import java.util.Stack;

public class DataPlaneManagerProxy {
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private Stack<Rollback> rollbacks;

    public DataPlaneManagerProxy(Stack<Rollback> rollbacks) {
        dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    public NetworkConfiguration createGoalState(Object arg) throws Exception {
        NetworkConfiguration message = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.createGoalState(message);

        CreateGoalStateRollback rollback = new CreateGoalStateRollback(dataPlaneManagerRestClient);
        rollback.createNetworkConfiguration(message);
        rollbacks.add(rollback);

        return message;
    }

    public void deleteGoalState(Object arg) throws Exception {
        NetworkConfiguration message = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.deleteGoalState(message);

        DeleteGoalStateRollback rollback = new DeleteGoalStateRollback(dataPlaneManagerRestClient);
        rollback.createNetworkConfiguration(message);
        rollbacks.add(rollback);
    }

    public NetworkConfiguration updateGoalState(Object arg) throws Exception {
        NetworkConfiguration message = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.updateGoalState(message);

        CreateGoalStateRollback rollback = new CreateGoalStateRollback(dataPlaneManagerRestClient);
        rollback.updateNetworkConfiguration(message);
        rollbacks.add(rollback);

        return message;
    }
}
