package org.demo;

import java.util.List;  
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener. ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;



public class ConsumerRunnable implements Runnable{

    //private static DefaultMQPushConsumer consumer = null;

    private String url = null;
    private String topicName = null;
    private String tagName = null;

    public ConsumerRunnable(String url, String topicName, String tagName,int size) {
        this.url = url;
        this.topicName = topicName;
        this.tagName = tagName;
        this.size = size;
    }


    private static String createSpecificSizeString(int size){
        byte[] temp = new byte[size];
        Arrays.fill(temp, (byte)0);
        String temp_str = new String(temp);
        return temp_str;
    }
    // private static void startConsumer(String topicName) throws Exception {
    //     System.out.println(topicName + " " + "Start consume");
    //     while (true) {
    //         // Wait for a message
    //         Message<byte[]> msg = consumer.receive();
    //         try {
    //             System.out.printf(topicName + " " + Thread.currentThread().getName() + "   Message from: %s\n", new String(msg.getData()));
    //             consumer.acknowledge(msg);
    //         } catch (Exception e) {
    //             System.err.printf("Unable to consume message: %s", e.getMessage());
    //             consumer.negativeAcknowledge(msg);
    //         }
    //     }
    // }

    @Override
    public void run(){
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumerGroup-"+topicName);
            // 设置NameServer的地址
            consumer.setNamesrvAddr(url);

    	    // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
            consumer.subscribe(topicName, tagName);
    	    // 注册回调实现类来处理从broker拉取回来的消息
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                    // 标记该消息已经被成功消费
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            consumer.start();
            System.out.printf("%s Consumer Started.%n", Thread.currentThread().getName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}