package org.demo;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerThreadPool {
    public static void main(String[] args) {
        int consumerThreadCount = 20;
        int topicNumber = 20;

        String url = "localhost:9876";
        String topicName = "topic";
        String tagName = "*";

        ExecutorService pool = Executors.newFixedThreadPool(consumerThreadCount);
        for (int topicIndex = 0; topicIndex < topicNumber; topicIndex++) {
            pool.submit(new ConsumerRunnable(url, topicName + Integer.toString(topicIndex), tagName));
        }
        pool.shutdown();
    }
}