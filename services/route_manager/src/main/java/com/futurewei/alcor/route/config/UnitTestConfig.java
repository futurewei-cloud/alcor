package com.futurewei.alcor.route.config;

public class UnitTestConfig {

    public static String projectId = "3dda2801-d675-4688-a63f-dcda8d327f50";
    public static String vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88038";
    public static String subnetId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String routeId = "90b5cce3-3bca-4d23-a8b6-71df44833ac7";
    public static String name = "test_subnet";
    public static String updateName = "update_subnet";
    public static String cidr = "10.0.0.0/16";
    public static String resource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"test_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String updateResource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"update_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String vpcResource = "{\"network\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"name\":\"test_vpc\",\"description\":\"\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String exception = "Request processing failed; nested exception is java.lang.Exception: com.futurewei.alcor.common.exception.ResourceNotFoundException: Subnet not found : 9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String createException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String createFallbackException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String macAddress = "00-AA-BB-CC-36-51";

    public static String routerId = "90b5cce3-3bca-4d23-a8b6-71df44833ac0";
    public static String updateRouterId = "90b5cce3-3bca-4d23-a8b6-71df44833ac1";
    public static String portId = "80b5cce3-3bca-4d23-a8b6-71df44833ac0";
    public static String diffPortId = "80b5cce3-3bca-4d23-a8b6-71df44833ab0";
    public static String routerExtraAttributeId = "90b5cce3-3bca-4d23-a8b6-71df44833ac0";
    public static String neutronRouterResource = "{\"router\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"90b5cce3-3bca-4d23-a8b6-71df44833ac0\",\"name\":\"router1\",\"admin_state_up\":true}}";
    public static String neutronRouterUpdateResource = "{\"router\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"90b5cce3-3bca-4d23-a8b6-71df44833ac1\",\"name\":\"router1\",\"admin_state_up\":true}}";
    public static String routerInterfaceRequest_port = "{\"port_id\":\"80b5cce3-3bca-4d23-a8b6-71df44833ac0\"}";
    public static String routerInterfaceRequest_subnet = "{\"subnet_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\"}";
    public static String routerInterfaceRequest_subnetAndPort = "{\"port_id\":\"80b5cce3-3bca-4d23-a8b6-71df44833ac0\", \"subnet_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\"}";
    public static String routesToNeutronRouterRequest = "{\"router\":{\"routes\":[{\"destination\":\"10.0.3.0/24\", \"nexthop\":\"10.0.0.13\"}]}}";
}
