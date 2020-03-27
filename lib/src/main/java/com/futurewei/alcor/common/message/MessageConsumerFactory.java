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

package com.futurewei.alcor.common.message;

import com.futurewei.alcor.common.config.IKafkaConfiguration;
import com.futurewei.alcor.common.repo.AbstractFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

public abstract class MessageConsumerFactory implements AbstractFactory<Consumer> {

    @Value("${apache.kafka.address}")
    private String kafkaAddress;

    public Consumer Create() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, IKafkaConfiguration.CONSUMER_GROUP_ID);

        // Key is set as long
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());

        Deserializer deserializer = getDeserializer();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass().getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, IKafkaConfiguration.MAX_POLL_RECORDS);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, IKafkaConfiguration.OFFSET_RESET_EARLIER);

        Consumer<Long, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }

    public abstract Deserializer getDeserializer();
}
