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

package com.futurewei.alcor.web.entity.node;
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