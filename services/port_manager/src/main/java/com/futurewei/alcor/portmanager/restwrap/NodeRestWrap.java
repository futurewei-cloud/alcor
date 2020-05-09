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

import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import com.futurewei.alcor.portmanager.utils.BeanUtil;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.web.rest.NodeRest;

import java.util.Stack;

public class NodeRestWrap {
    private NodeRest nodeRest;
    private Stack<PortStateRollback> rollbacks;

    public NodeRestWrap(Stack<PortStateRollback> rollbacks) {
        nodeRest = BeanUtil.getBean(NodeRest.class);
        this.rollbacks = rollbacks;
    }

    public NodeInfoJson verifyHost(String nodeId) throws Exception {
        return nodeRest.getNodeInfo(nodeId);
    }
}
