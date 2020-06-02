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

import com.futurewei.alcor.portmanager.util.SpringContextUtil;
import com.futurewei.alcor.web.entity.port.*;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.portmanager.restclient.VpcManagerRestClient;
import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import java.util.Stack;

public class VpcManagerProxy {
    private VpcManagerRestClient vpcManagerRestClient;
    private Stack<PortStateRollback> rollbacks;

    public VpcManagerProxy(Stack<PortStateRollback> rollbacks) {
        vpcManagerRestClient = SpringContextUtil.getBean(VpcManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    /**
     * Verify if the vpc of vpcId exists
     * @param args PortState
     * @return The information of vpc
     * @throws Exception Rest request exception
     */
    public VpcWebJson verifyVpc(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        return vpcManagerRestClient.verifyVpc(portEntity.getProjectId(), portEntity.getVpcId());
    }
}
