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
