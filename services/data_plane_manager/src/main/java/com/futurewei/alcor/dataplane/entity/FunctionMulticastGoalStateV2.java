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
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;

import java.util.ArrayList;
import java.util.List;

public class FunctionMulticastGoalStateV2 extends MulticastGoalStateV2{
    private List<String> topics;
    private List<String> nextTopics;

    public FunctionMulticastGoalStateV2(Goalstate.GoalStateV2 goalState, List<String> topics) {
        super(goalState);
        this.topics = topics;
        this.nextTopics = new ArrayList<>();
    }

    public FunctionMulticastGoalStateV2(Goalstate.GoalStateV2 goalState, List<String> topics, List<String> nextTopics) {
        super(goalState);
        this.topics = topics;
        this.nextTopics = nextTopics;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getNextTopics() {
        return nextTopics;
    }

    public void setNextTopics(List<String> nextTopics) {
        this.nextTopics = nextTopics;
    }

    public MulticastGoalStateByte getMulticastGoalStateByte() {
        MulticastGoalStateByte multicastGoalStateByte = new MulticastGoalStateByte();
        multicastGoalStateByte.setNextTopics(this.topics);
        multicastGoalStateByte.setNextSubTopics(this.nextTopics);
        multicastGoalStateByte.setGoalStateByte(this.getGoalState().toByteArray());

        return multicastGoalStateByte;
    }
}
