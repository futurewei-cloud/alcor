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
package com.futurewei.alcor.portmanager.restwrap;

import com.futurewei.alcor.portmanager.utils.BeanUtil;
import com.futurewei.alcor.web.entity.PortState;
import com.futurewei.alcor.web.entity.VpcStateJson;
import com.futurewei.alcor.web.rest.VpcRest;
import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import java.util.Stack;

public class VpcRestWrap {
    private VpcRest vpcRest;
    private Stack<PortStateRollback> rollbacks;

    public VpcRestWrap(Stack<PortStateRollback> rollbacks) {
        vpcRest = BeanUtil.getBean(VpcRest.class);
        this.rollbacks = rollbacks;
    }

    public VpcStateJson verifyVpc(Object args) throws Exception {
        PortState portState = (PortState)args;
        return vpcRest.verifyVpc(portState.getProjectId(), portState.getVpcId());
    }
}
