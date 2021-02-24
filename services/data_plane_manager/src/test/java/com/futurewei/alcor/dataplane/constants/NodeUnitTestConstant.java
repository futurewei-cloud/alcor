package com.futurewei.alcor.dataplane.constants;

public class NodeUnitTestConstant {
    public static String url = "/node-info";

    public static String node_id = "h01";
    public static String create_node_test_input = "" +
            "{\n" +
            "    \"host_info\":{\n" +
            "    \"node_id\":\"h01\",\n" +
            "            \"node_name\":\"host1\",\n" +
            "            \"local_ip\":\"10.0.0.1\",\n" +
            "            \"mac_address\":\"AA-BB-CC-01-01-01\",\n" +
            "            \"veth\":\"eth0\",\n" +
            "            \"server_port\":50001,\n" +
            "            \"host_dvr_mac\":null,\n" +
            "            \"unicast_topic\":\"unicast-topic-1\",\n" +
            "            \"group_topic\":\"group-topic-1\"\n" +
            "    }\n" +
            "}";

}
