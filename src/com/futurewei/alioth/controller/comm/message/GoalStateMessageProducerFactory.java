package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.comm.serialization.GoalStateSerializer;
import org.apache.kafka.common.serialization.Serializer;

public class GoalStateMessageProducerFactory extends MessageProducerFactory {
    @Override
    public Serializer getSerializer() {
        return new GoalStateSerializer();
    }
}
