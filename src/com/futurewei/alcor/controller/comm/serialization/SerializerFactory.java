package com.futurewei.alcor.controller.comm.serialization;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class SerializerFactory{

    public static Serializer Create(SerializerType type) {
        Serializer serializer = null;
        switch (type) {
            case STRING:
                serializer = new StringSerializer();
                break;

            case PROTOBUF:
                serializer = new GoalStateSerializer();
                break;

            default:
                throw new UnsupportedOperationException("Unsupported serializer type: " + type.name());
        }

        return serializer;
    }
}
