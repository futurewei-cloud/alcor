/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
