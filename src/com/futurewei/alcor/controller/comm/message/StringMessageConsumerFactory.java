package com.futurewei.alcor.controller.comm.message;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class StringMessageConsumerFactory extends MessageConsumerFactory {
    @Override
    public Deserializer getDeserializer() {
        return new StringDeserializer();
    }
}
