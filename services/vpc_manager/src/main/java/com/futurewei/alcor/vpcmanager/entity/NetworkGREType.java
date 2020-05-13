package com.futurewei.alcor.vpcmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Map;

@Data
public class NetworkGREType {

    @Id
    private Long key;

    @JsonProperty("gre_id")
    private String greId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("mtu")
    private Integer mtu;

    @JsonProperty("segment_id")
    private String segmentId;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;


    public NetworkGREType() {
    }

}
