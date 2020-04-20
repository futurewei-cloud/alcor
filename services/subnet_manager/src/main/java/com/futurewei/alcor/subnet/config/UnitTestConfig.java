package com.futurewei.alcor.subnet.config;

public class UnitTestConfig {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88038";
    public static String subnetId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String name = "test_subnet";
    public static String updateName = "update_subnet";
    public static String cidr = "10.0.0.0/16";
    public static String resource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"test_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String updateResource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"update_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String exception = "Request processing failed; nested exception is java.lang.Exception: com.futurewei.alcor.common.exception.ResourceNotFoundException: Subnet not found : 9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String createException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String createException2 = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String macAddress = "00-AA-BB-CC-36-51";
}
