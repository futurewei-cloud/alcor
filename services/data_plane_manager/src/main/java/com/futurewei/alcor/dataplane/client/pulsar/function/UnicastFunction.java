/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.dataplane.client.pulsar.function;

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
