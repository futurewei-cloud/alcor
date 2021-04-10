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
package com.futurewei.alcor.nodemanager.utils;

public class NodeManagerConstant {
    public static final String JSON_HOSTS = "host_infos";
    public static final String JSON_ID1 = "node_id";
    public static final String JSON_NAME = "node_name";
    public static final String JSON_IP1 = "local_ip";
    public static final String JSON_MAC1 = "mac_address";
    public static final String JSON_VETH1 = "veth";
    public static final int GRPC_SERVER_PORT = 50001;
    public static final String UNICAST_TOPIC = "unicast_topic";
    public static final String MULTICAST_TOPIC = "multicast_topic";
    public static final String GROUP_TOPIC = "group_topic";
    public static final String JSON_NCM_ID = "ncm_id";
    public static final String JSON_NCM_URI = "ncm_uri";

    //Exception Messages
    public static final String NODE_EXCEPTION_PARAMETER_NULL_EMPTY = "Parameter is null or empty";
    public static final String NODE_EXCEPTION_NODE_NOT_EXISTING = "The node to update or delete is not existing.";
    public static final String NODE_EXCEPTION_NODE_ALREADY_EXISTING = "The node to create is already existing.";
    public static final String NODE_EXCEPTION_FILE_EMPTY = "The file is empty";
    public static final String NODE_EXCEPTION_JSON_EMPTY = "The json is empty";
    public static final String NODE_EXCEPTION_IP_FORMAT_INVALID = "Invalid IP address format";
    public static final String NODE_EXCEPTION_MAC_FORMAT_INVALID = "Invalid MAC address format";
    public static final String NODE_EXCEPTION_REPOSITORY_EXCEPTION = "There is an error for service to call repository";
}