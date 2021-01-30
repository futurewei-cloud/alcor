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
package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.web.entity.gateway.ZetaPortEntity;

import java.util.List;

public class ZetaPortGoalState {
    private OperationType opType;
    private List<ZetaPortEntity> portEntities;

    public ZetaPortGoalState() {

    }

    public ZetaPortGoalState(OperationType opType, List<ZetaPortEntity> portEntities) {
        this.opType = opType;
        this.portEntities = portEntities;
    }

    public OperationType getOpType() {
        return opType;
    }

    public void setOpType(OperationType opType) {
        this.opType = opType;
    }

    public List<ZetaPortEntity> getPortEntities() {
        return portEntities;
    }

    public void setPortEntities(List<ZetaPortEntity> portEntities) {
        this.portEntities = portEntities;
    }
}
