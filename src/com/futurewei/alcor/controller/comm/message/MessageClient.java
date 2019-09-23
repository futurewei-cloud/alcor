package com.futurewei.alcor.controller.comm.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class MessageClient {

    private final MessageConsumerFactory messageConsumerFactory;
    private final MessageProducerFactory messageProducerFactory;

    public MessageClient(MessageConsumerFactory messageConsumerFactory, MessageProducerFactory messageProducerFactory) {
        this.messageConsumerFactory = messageConsumerFactory;
        this.messageProducerFactory = messageProducerFactory;
    }

    public List<?> runConsumer(String topic, boolean keepRunning) {
        if(this.messageConsumerFactory == null){
            System.out.printf("No message consumer factory is specified");
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
                System.out.println("No message found :" + noMessageFound);

                if (noMessageFound > IKafkaConfiguration.MAX_NO_MESSAGE_FOUND_COUNT)
                    // If no message found count is reached to threshold exit loop.
                    break;
                else
                    continue;
            }

            //print each record.
            consumerRecords.forEach(record -> {
                System.out.println("Record Key " + record.key());
                System.out.println("Record value " + record.value());
                System.out.println("Record partition " + record.partition());
                System.out.println("Record offset " + record.offset());

                recordsValue.add(record.value());
            });

            // commits the offset of record to broker.
            consumer.commitAsync();
        }

        consumer.close();
        return recordsValue;
    }

    public void runProducer(String topic, Object message, int messageCount) {
        if(this.messageProducerFactory == null){
            System.out.printf("No message producer factory is specified");
            return;
        }

        Producer producer = this.messageProducerFactory.Create();

        for (int index = 0; index < messageCount; index++) {

            ProducerRecord<Long, Object> record = new ProducerRecord(topic, message);
            try {
                RecordMetadata metadata = (RecordMetadata) producer.send(record).get();
                System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
                        + " with offset " + metadata.offset());
            }
            catch (ExecutionException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
            catch (InterruptedException e) {
                System.out.println("Error in sending record");
                System.out.println(e);
            }
        }
    }

    public void runProducer(String topic, Object message) {
        this.runProducer(topic, message, 1);
    }

    // TODO: Determine topic format
    public static String getGoalStateTopic(String id){
        return "Host-" + id;
    }
}
