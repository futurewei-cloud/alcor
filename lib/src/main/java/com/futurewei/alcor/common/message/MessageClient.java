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
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class MessageClient {

    private final MessageConsumerFactory messageConsumerFactory;
    private final MessageProducerFactory messageProducerFactory;

    public MessageClient(MessageConsumerFactory messageConsumerFactory, MessageProducerFactory messageProducerFactory) {
        this.messageConsumerFactory = messageConsumerFactory;
        this.messageProducerFactory = messageProducerFactory;
    }

    // TODO: Determine topic format
    public static String getGoalStateTopic(String id) {
        return "Host-" + id;
    }

    public List<?> runConsumer(String topic, boolean keepRunning) {
        Logger logger = LoggerFactory.getLogger();

        if (this.messageConsumerFactory == null) {
            logger.log(Level.INFO, "No message consumer factory is specified");
            return null;
        }

        List recordsValue = new ArrayList();
        Consumer consumer = this.messageConsumerFactory.Create();
        consumer.subscribe(Collections.singletonList(topic));

        int noMessageFound = 0;
        while (keepRunning) {
            // 1000 milliseconds is the time consumer will wait if no record is found at broker.
            ConsumerRecords<Long, ?> consumerRecords = consumer.poll(1000);

            if (consumerRecords.count() == 0) {
                noMessageFound++;
                logger.log(Level.INFO, "No message found :" + noMessageFound);

                if (noMessageFound > IKafkaConfiguration.MAX_NO_MESSAGE_FOUND_COUNT)
                    // If no message found count is reached to threshold exit loop.
                    break;
                else
                    continue;
            }

            //print each record.
            consumerRecords.forEach(record -> {
                logger.log(Level.INFO, "Record Key " + record.key());
                logger.log(Level.INFO, "Record value " + record.value());
                logger.log(Level.INFO, "Record partition " + record.partition());
                logger.log(Level.INFO, "Record offset " + record.offset());

                recordsValue.add(record.value());
            });
            // commits the offset of record to broker.
            consumer.commitAsync();
        }

        consumer.close();
        return recordsValue;
    }

    public void runProducer(String topic, Object message, int messageCount) {
        Logger logger = LoggerFactory.getLogger();
        if (this.messageProducerFactory == null) {
            logger.log(Level.INFO, "No message producer factory is specified");
            return;
        }

        Producer producer = this.messageProducerFactory.Create();

        for (int index = 0; index < messageCount; index++) {

            ProducerRecord<Long, Object> record = new ProducerRecord(topic, message);
            try {
                RecordMetadata metadata = (RecordMetadata) producer.send(record).get();
                logger.log(Level.INFO, "Record sent with key " + index + " to partition " + metadata.partition()
                        + " with offset " + metadata.offset());
            } catch (ExecutionException e) {
                logger.log(Level.SEVERE, "Error in sending record", e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error in sending record", e);
            }
        }
    }

    public void runProducer(String topic, Object message) {
        this.runProducer(topic, message, 1);
    }
}
