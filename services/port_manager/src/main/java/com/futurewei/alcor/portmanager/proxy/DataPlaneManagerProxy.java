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
import com.futurewei.alcor.portmanager.rollback.CreateNetworkConfigRollback;
import com.futurewei.alcor.portmanager.rollback.DeleteNetworkConfigRollback;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import java.util.Stack;

public class DataPlaneManagerProxy {
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private Stack<Rollback> rollbacks;

    public DataPlaneManagerProxy(Stack<Rollback> rollbacks) {
        dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    public NetworkConfiguration createNetworkConfig(Object arg) throws Exception {
        NetworkConfiguration networkConfiguration = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.createNetworkConfig(networkConfiguration);

        CreateNetworkConfigRollback rollback = new CreateNetworkConfigRollback(dataPlaneManagerRestClient);
        rollback.createNetworkConfig(networkConfiguration);
        rollbacks.add(rollback);

        return networkConfiguration;
    }

    public void deleteNetworkConfig(Object arg) throws Exception {
        NetworkConfiguration networkConfiguration = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.deleteNetworkConfig(networkConfiguration);

        DeleteNetworkConfigRollback rollback = new DeleteNetworkConfigRollback(dataPlaneManagerRestClient);
        rollback.deleteNetworkConfig(networkConfiguration);
        rollbacks.add(rollback);
    }

    public NetworkConfiguration updateNetworkConfig(Object arg) throws Exception {
        NetworkConfiguration networkConfiguration = (NetworkConfiguration)arg;
        dataPlaneManagerRestClient.updateNetworkConfig(networkConfiguration);

        CreateNetworkConfigRollback rollback = new CreateNetworkConfigRollback(dataPlaneManagerRestClient);
        rollback.updateNetworkConfig(networkConfiguration);
        rollbacks.add(rollback);

        return networkConfiguration;
    }
}
