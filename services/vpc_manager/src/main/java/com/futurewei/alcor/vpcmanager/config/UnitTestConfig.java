package com.futurewei.alcor.vpcmanager.config;

public class UnitTestConfig {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88038";
    public static String subnetId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String segmentId = "9192a4d4-ffff-4ece-b3f0-8d36e3d87000";
    public static String segmentRangeId = "9192a4d4-ffff-4ece-b3f0-8d36e3d86000";
    public static String networkType = "vlan";
    public static String name = "test_subnet";
    public static String updateName = "update_subnet";
    public static String cidr = "10.0.0.0/16";
    public static String resource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"test_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String vpcResource = "{\"network\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"name\":\"test_network\",\"description\":\"\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String segmentResource = "{\"segment\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d87000\",\"name\":\"test_segment\",\"description\":\"\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\"}}";
    public static String segmentRangeResource = "{\"network_segment_range\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d86000\",\"name\":\"test_segment\",\"description\":\"\",\"network_type\":\"vlan\"}}";
    public static String updateResource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"update_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String exception = "Request processing failed; nested exception is java.lang.Exception: com.futurewei.alcor.common.exception.ResourceNotFoundException: Subnet not found : 9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String createException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String createFallbackException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String macAddress = "00-AA-BB-CC-36-51";

}
