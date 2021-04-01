/*Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.nodemanager.utils;

public class NodeManagerConstant {
    public static final String JSON_HOSTS = "host_infos";
    public static final String JSON_ID1 = "node_name";
    public static final String JSON_IP1 = "local_ip";
    public static final String JSON_MAC1 = "mac_address";
    public static final String JSON_VETH1 = "veth";
    public static final int GRPC_SERVER_PORT = 50001;
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

