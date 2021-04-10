/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
    public static String segmentResource = "{\"segment\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d87000\",\"segmentation_id\":\"1\",\"network_type\":\"vlan\",\"network_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\"}}";
    public static String segmentRangeResource = "{\"network_segment_range\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d86000\",\"name\":\"test_segment\",\"description\":\"\",\"network_type\":\"vlan\"}}";
    public static String updateResource = "{\"subnet\":{\"project_id\":\"3dda2801-d675-4688-a63f-dcda8d327f50\",\"vpc_id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88038\",\"id\":\"9192a4d4-ffff-4ece-b3f0-8d36e3d88000\",\"name\":\"update_subnet\",\"cidr\":\"10.0.0.0/16\"}}";
    public static String exception = "Request processing failed; nested exception is java.lang.Exception: com.futurewei.alcor.common.exception.ResourceNotFoundException: Subnet not found : 9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    public static String createException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String createFallbackException = "Request processing failed; nested exception is java.lang.Exception: java.util.concurrent.CompletionException: java.util.concurrent.CompletionException: com.futurewei.alcor.common.exception.FallbackException: fallback request";
    public static String macAddress = "00-AA-BB-CC-36-51";

}
