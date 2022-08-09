/*
 *
 * MIT License
 * Copyright(c) 2020 Futurewei Cloud
 *
 *     Permission is hereby granted,
 *     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
 *     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
 *     to whom the Software is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;

public class FunctionUnicastGoalStateV2 extends UnicastGoalStateV2{
    private String topic;
    private String subTopic;

    public FunctionUnicastGoalStateV2(Goalstate.GoalStateV2 goalState) {
        super(goalState);
    }

    public FunctionUnicastGoalStateV2(Goalstate.GoalStateV2 goalState, String topic) {
        super(goalState);
        this.topic = topic;
    }

    public FunctionUnicastGoalStateV2(Goalstate.GoalStateV2 goalState, String topic, String subTopic) {
        super(goalState);
        this.topic = topic;
        this.subTopic = subTopic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }

    public UnicastGoalStateByte getUnicastGoalStateByte() {
        UnicastGoalStateByte unicastGoalStateByte = new UnicastGoalStateByte();
        unicastGoalStateByte.setNextTopic(this.topic);
        unicastGoalStateByte.setNextSubTopic(this.subTopic);
        unicastGoalStateByte.setGoalStateByte(this.getGoalState().toByteArray());
        return unicastGoalStateByte;
    }
}
