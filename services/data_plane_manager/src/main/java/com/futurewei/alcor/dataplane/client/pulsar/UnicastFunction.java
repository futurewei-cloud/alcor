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

import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicastFunction implements Function<UnicastGoalStateByte, UnicastGoalStateByte> {
    private static final Logger LOG = LoggerFactory.getLogger(UnicastFunction.class);

    @Override
    public UnicastGoalStateByte process(UnicastGoalStateByte unicastGoalStateByte, Context context) throws Exception {
        LOG.info("Receive UnicastGoalState from topic:{}, unicastGoalState:{}",
                context.getInputTopics(), unicastGoalStateByte);

        String nextTopic = unicastGoalStateByte.getNextTopic();
        if (nextTopic == null || "".equals(nextTopic)) {
            LOG.warn("Next topic of unicastGoalState is null, do nothing");
            return unicastGoalStateByte;
        }

        LOG.info("Publish unicastGoalState to nextTopic:{}", nextTopic);

        context.newOutputMessage(nextTopic, Schema.BYTES)
                .value(unicastGoalStateByte.getGoalStateByte())
                .sendAsync();

        return unicastGoalStateByte;
    }
}
