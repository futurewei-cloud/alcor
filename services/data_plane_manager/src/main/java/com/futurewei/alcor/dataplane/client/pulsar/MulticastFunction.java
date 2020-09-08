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
package com.futurewei.alcor.dataplane.client.pulsar;

import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MulticastFunction implements Function<MulticastGoalState, MulticastGoalState> {
    private static final Logger LOG = LoggerFactory.getLogger(MulticastFunction.class);

    @Override
    public MulticastGoalState process(MulticastGoalState multicastGoalState, Context context) throws Exception {
        LOG.info("Receive MulticastGoalState from topic:{}, multicastGoalState:{}",
                context.getInputTopics(), multicastGoalState);

        List<String> nextTopics = multicastGoalState.getNextTopics();
        if (nextTopics == null || nextTopics.size() == 0) {
            LOG.warn("Next topics of multicastGoalState is null, do nothing");
            return multicastGoalState;
        }

        for (String nextTopic: nextTopics) {
            LOG.info("Publish multicastGoalState to nextTopic:{}", nextTopic);
            context.publish(nextTopic, multicastGoalState);
        }

        return multicastGoalState;
    }
}
