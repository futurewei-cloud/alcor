/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

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
