
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
package com.futurewei.alcor.web.entity;
public class NodeManagerConstant {
    public static final int NODE_INFO_FILE = 1;
    public static final int NODE_INFO_REPOSITOTY = 2;
    public static final int GRPC_SERVER_PORT = 50001;
    //Exception Messages
    public static final String NODE_EXCEPTION_PARAMETER_NULL_EMPTY = "Parameter is null or empty";
    public static final String NODE_EXCEPTION_NODE_IP_INVALID = "Invalid ip address";
    public static final String NODE_EXCEPTION_NODE_NOT_EXISTING = "The node to update or delete is not existing.";
    public static final String NODE_EXCEPTION_NODE_ALREADY_EXISTING = "The node to create is already existing.";
    public static final String NODE_EXCEPTION_FILE_EMPTY = "The file is empty";
}