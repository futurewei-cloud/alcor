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
