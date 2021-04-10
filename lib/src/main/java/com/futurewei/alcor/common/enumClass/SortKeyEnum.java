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


package com.futurewei.alcor.common.enumClass;

public enum SortKeyEnum {

    CIDR("cidr"),
    ENABLE_DHCP("enable_dhcp"),
    GATEWAY_IP("gateway_ip"),
    ID("id"),
    IP_VERSION("ip_version"),
    IPV6ADDRMODE("ipv6_address_mode"),
    IPV6RAMODE("ipv6_ra_mode"),
    NAME("name"),
    NETWORK_ID("network_id"),
    SEGMENT_ID("segment_id"),
    SUBNETPOOL_ID("subnetpool_id"),
    TENANT_ID("tenant_id"),
    PROJECT_ID("project_id");

    private String sortKey;

    SortKeyEnum (String env) {
        this.sortKey = env;
    }

    public String getSortKey () {
        return sortKey;
    }
}
