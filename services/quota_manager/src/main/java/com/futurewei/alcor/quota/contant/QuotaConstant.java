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

package com.futurewei.alcor.quota.contant;

public class QuotaConstant {

    public static final String FLOATING_IP = "floating_ip";
    public static final String NETWORK = "network";
    public static final String PORT = "port";
    public static final String RBAC_POLICY = "rbac_policy";
    public static final String ROUTER = "router";
    public static final String SECURITY_GROUP = "security_group";
    public static final String SECURITY_GROUP_RULE = "security_group_rule";
    public static final String SUBNET = "subnet";
    public static final String SUBNETPOOL = "subnetpool";


    public static final String[] RESOURCES = new String[] {FLOATING_IP, NETWORK, PORT, RBAC_POLICY,
            ROUTER, SECURITY_GROUP, SECURITY_GROUP_RULE, SUBNET, SUBNETPOOL};
}
