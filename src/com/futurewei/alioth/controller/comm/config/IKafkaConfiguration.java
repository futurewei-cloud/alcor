package com.futurewei.alioth.controller.comm.config;

public interface IKafkaConfiguration {
    public static String KAFKA_BROKERS = "10.213.43.188:9092";

    public static String CONSUMER_GROUP_ID = "vpc_controller_1";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT = 10;

    public static String PRODUCER_CLIENT_ID = "vpc_controller";

    public static String OFFSET_RESET_LATEST = "latest";
    public static String OFFSET_RESET_EARLIER = "earliest";
    public static Integer MAX_POLL_RECORDS = 1;
}
