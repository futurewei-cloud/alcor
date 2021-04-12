package com.futurewei.alcor.dataplane.config;

import java.util.ArrayList;
import java.util.List;

public class ClientConstant {
    public static String fastPath = "gRPC";
    public static String mqPath = "pulsar";

    public static List<String> clientChoices = new ArrayList<>() {
        {
            add(fastPath);
            add(mqPath);
        }
    };

    public static int X = 100;
    public static int Y = 90;
}