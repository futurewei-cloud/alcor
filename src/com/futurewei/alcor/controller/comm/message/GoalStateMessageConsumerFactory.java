package com.futurewei.alcor.controller.comm.message;

import com.futurewei.alcor.controller.comm.serialization.GoalStateDeserializer;
import org.apache.kafka.common.serialization.Deserializer;

public class GoalStateMessageConsumerFactory extends MessageConsumerFactory {
    @Override
    public Deserializer getDeserializer() {
        return new GoalStateDeserializer();
    }
}
