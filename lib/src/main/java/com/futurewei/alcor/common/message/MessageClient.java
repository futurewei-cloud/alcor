/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
