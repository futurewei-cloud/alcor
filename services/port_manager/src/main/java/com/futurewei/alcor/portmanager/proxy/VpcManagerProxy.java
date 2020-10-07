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
import com.futurewei.alcor.portmanager.exception.GetVpcEntityException;
import com.futurewei.alcor.web.entity.port.*;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.web.restclient.VpcManagerRestClient;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import java.util.Stack;

public class VpcManagerProxy {
    private VpcManagerRestClient vpcManagerRestClient;
    private Stack<Rollback> rollbacks;

    public VpcManagerProxy(Stack<Rollback> rollbacks) {
        vpcManagerRestClient = SpringContextUtil.getBean(VpcManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    /**
     * Verify if the vpc of vpcId exists
     * @param args PortEntity
     * @return The information of vpc
     * @throws Exception Rest request exception
     */
    public VpcEntity getVpcEntity(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        VpcWebJson vpcWebJson = vpcManagerRestClient.getVpc(portEntity.getProjectId(), portEntity.getVpcId());
        if (vpcWebJson == null || vpcWebJson.getNetwork() == null) {
            throw new GetVpcEntityException();
        }

        return vpcWebJson.getNetwork();
    }
}
