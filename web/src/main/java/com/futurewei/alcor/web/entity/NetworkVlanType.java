package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

@Data
public class NetworkVlanType {

    @Id
    private Long key; // Valid value range: 1 - 4094, 0 and 4095 are reserved

    @JsonProperty("vlan_id")
    private String vlanId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("mtu")
    private Integer mtu; // 1500 - 18190

    @JsonProperty("status")
    private String status; // active/suspend

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public NetworkVlanType() { }

}
