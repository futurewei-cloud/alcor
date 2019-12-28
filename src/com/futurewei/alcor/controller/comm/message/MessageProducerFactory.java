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

package com.futurewei.alcor.controller.comm.message;

import com.futurewei.alcor.controller.interfaces.AbstractFactory;
import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

public abstract class MessageProducerFactory implements AbstractFactory<Producer> {

    @Value("${apache.kafka.address}")
    private String kafkaAddress;

    public Producer Create() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaAddress);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, IKafkaConfiguration.PRODUCER_CLIENT_ID);

        // Key is set as long and Value is given by concrete implementation
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());

        Serializer serializer = getSerializer();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer.getClass().getName());

        //TODO: Optimizing partition
        // props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());

        return new KafkaProducer<>(props);
    }

    public abstract Serializer getSerializer();
}
