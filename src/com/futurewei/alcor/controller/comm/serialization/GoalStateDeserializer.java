package com.futurewei.alcor.controller.comm.serialization;

import com.futurewei.alcor.controller.schema.Goalstate.GoalState;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class GoalStateDeserializer implements Deserializer<GoalState> {

    public GoalStateDeserializer() {
    }

    public GoalState deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : GoalState.parseFrom(data);

        } catch(InvalidProtocolBufferException bf_exp) {
            throw new SerializationException("Error when deserializing byte[] to string due to invalid protobuf exception " + bf_exp);
        }
    }
}