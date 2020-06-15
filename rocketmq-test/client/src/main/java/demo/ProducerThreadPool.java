package org.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProducerThreadPool {
    public static void main(String[] args) {
        int producerThreadNumber = 20;
        int topicNumber = 20;

        String url = "localhost:9876";
        String topicName = "topic";
        int sleepTime = 1000;

        ExecutorService pool = Executors.newFixedThreadPool(producerThreadNumber);
        for (int topicIndex = 0; topicIndex < topicNumber; topicIndex++) {
            pool.submit(new ProducerRunnable(url, topicName + Integer.toString(topicIndex), sleepTime));
        }
        pool.shutdown();
    }
}