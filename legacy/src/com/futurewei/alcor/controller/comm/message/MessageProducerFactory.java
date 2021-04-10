/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.controller.comm.message;

import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import com.futurewei.alcor.controller.interfaces.AbstractFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
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
