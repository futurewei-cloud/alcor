package org.demo;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import javax.swing.*;

public class ProducerRunnable implements Runnable{

    //private static DefaultMQProducer producer;

    private String url = null;
    private String topicName = null;
    private int sleepTime = 0;
    private int size = 0;

    public ProducerRunnable(String url, String topicName, int sleepTime,int size) {
        this.url = url;
        this.topicName = topicName;
        this.sleepTime = sleepTime;
        this.size = size;
    }

    private static String createSpecificSizeString(int size){
        byte[] temp = new byte[size];
        Arrays.fill(temp, (byte)0);
        String temp_str = new String(temp);
        return temp_str;
    }    

    private static void startProducer(String topicName,DefaultMQProducer producer) throws Exception {
        System.out.println(topicName + " " + "Start produce");
        while (true) {
            Message message = new Message(topicName,"TagTest",createSpecificSizeString(size).getBytes());
            long startTime = System.currentTimeMillis();
            producer.send(message);
            long endTime = System.currentTimeMillis();
            System.out.println(topicName + " " + "send message in %d",endTime-startTime);
            Thread.sleep(1000);
        }
    }

    @Override
    public void run(){
        try {
            //设置生产者组名
            DefaultMQProducer producer = new DefaultMQProducer("producerGroup-"+ topicName);
            //指定nameServer的地址, 多个地址用分号分隔
            producer.setNamesrvAddr(url);
            
            //启动实例
            producer.start();

            
    
            startProducer(topicName,producer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}