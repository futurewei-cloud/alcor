package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

@Data
public class NetworkVxlanType {

    @Id
    private Long key;

    @JsonProperty("vxlan_id")
    private String vxlanId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("mtu")
    private Integer mtu;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public NetworkVxlanType() {
    }

}
